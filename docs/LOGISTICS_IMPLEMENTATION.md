# Logistics Implementation: What Was Done

This doc explains the four main areas that were implemented so you can understand the flow and where things live in the codebase.

---

## 1. Inventory reservation during checkout

**Problem before:** Checkout created an order and **immediately deducted** stock. If the user never paid or payment failed, the stock was already gone and had to be fixed manually.

**What we did:** Stock is **reserved** when the user goes to checkout. It is only **converted to a real sale** when payment succeeds. If payment fails or the user abandons, the reservation is **released** (or expires after 30 minutes).

### Flow

```
User clicks "Checkout" / "Pay"
    → Create Order (status PENDING)
    → Reserve inventory for each line (by variant, across locations) with reference "ORDER" + orderPublicId
    → Cart cleared
    → User is sent to payment gateway

Payment SUCCESS (via verify endpoint or Paystack webhook)
    → Convert reservation to sale (reserved qty becomes sold, movement type SALE)
    → Order status → PAID
    → Create delivery order(s) if address provided

Payment FAILURE or user never pays
    → Release reservation (put stock back to available)
    → Order can stay PENDING or be cancelled

Expired (e.g. 30 min no payment)
    → Scheduled job runs every 5 min
    → Finds PENDING orders with reservation_expires_at < now
    → Releases their reservation and sets order to CANCELLED
```

### Key pieces

| What | Where |
|------|--------|
| Reserve by reference (ORDER + orderId) | `InventoryService.reserveVariantQuantity()`, `reserve(..., referenceType, referenceId)` |
| Release all reserved for a reference | `InventoryService.releaseByReference(storeId, "ORDER", orderPublicId)` |
| Turn reservation into sale (payment success) | `InventoryService.convertReservationToSale(storeId, "ORDER", orderPublicId)` |
| Checkout: reserve instead of deduct | `CheckoutService.createOrderFromCart()` – creates order, sets `reservationExpiresAt`, then reserves per line |
| Payment success: convert + set PAID | `PaymentService.verify()` and `PaystackWebhookService.handleChargeSuccess()` |
| Payment failure: release | `PaymentService.verify()` when gateway says failed |
| Expiry job | `ReservationExpiryJob` (scheduled, releases expired PENDING orders) |
| Cancel checkout (user abandons) | `POST .../checkout/orders/{orderPublicId}/cancel-checkout` → releases reservation, cancels order |
| Order expiry field | `Order.reservationExpiresAt` (e.g. now + 30 min) |
| Lock to avoid two checkouts taking same stock | `InventoryItemRepository.findByStoreIdAndProductVariantIdForUpdate()` (pessimistic lock) used when allocating |

### DB

- `orders.reservation_expires_at` – when the reservation is no longer valid (migration V46).

---

## 2. Order splitting across locations

**Problem before:** One order had a flat list of lines. Stock was deducted “from somewhere” (highest available first) but we didn’t record **which location** fulfilled which line. One order = one delivery only.

**What we did:** When we reserve at checkout, we **allocate each line to specific location(s)** and persist that. We introduce **Shipments** (one per order + location) and **OrderItemAllocation** (order_item_id, location_id, quantity). Payment success then creates **one delivery per shipment** (so one order can have multiple deliveries).

### Flow

```
Checkout (create order + reserve):
    1. Create Order + OrderItems (no stock change yet).
    2. For each order line (variant + quantity), with lock:
       - Get inventory items for that variant (by location, sorted by available).
       - Allocate: take from first location(s) until quantity is covered.
       - Create OrderItemAllocation(orderItem, location, quantity) for each chunk.
       - Collect distinct locations used.
    3. Create one Shipment per distinct location (order_id, location_id, status PENDING).
    4. For each allocation: reserve at that location (inventory item = variant + location) with "ORDER" + orderPublicId.
    5. Clear cart.

Payment success:
    - Convert reservation to sale (same as before; by reference "ORDER" + orderPublicId).
    - For each Shipment of the order: create one DeliveryOrder (with shipment_id).
    - Set Shipment.deliveryOrderId = created delivery id.
```

So: **one order → many order items → allocations per location → one shipment per location → one delivery per shipment.**

### Key pieces

| What | Where |
|------|--------|
| Allocation: which location fulfills which qty | `CheckoutService.createOrderFromCart()` – uses `getAllocationCandidatesForVariant()` (locked), builds allocations + shipments |
| Store allocation per (order item, location, qty) | Entity `OrderItemAllocation`; repo `OrderItemAllocationRepository` |
| One group per (order, location) | Entity `Shipment` (order_id, location_id, status, delivery_order_id) |
| Reserve at specific location | For each allocation: `getOrCreateInventoryItem(storeId, variantPublicId, locationPublicId)` then `reserve(..., "ORDER", orderPublicId)` |
| Create one delivery per shipment | `PaymentService.verify()` and webhook: loop `ShipmentRepository.findByOrder_IdOrderByCreatedAtAsc(order.getId())`, create `CreateDeliveryOrderRequest` with `shipmentId`, then set `shipment.setDeliveryOrderId(d.getId())` |
| Delivery linked to shipment | `DeliveryOrder.shipmentId`; unique index so one delivery per shipment. Multiple deliveries per order allowed (index on order_id no longer unique). |

### DB

- **shipments** – id, order_id, location_id, status, delivery_order_id (V47).
- **order_item_allocations** – id, order_item_id, location_id, quantity (V47).
- **delivery_orders** – added `shipment_id`; unique on shipment_id; order_id non-unique (V48).

---

## 3. Delivery routing

**Problem before:** Deliveries were assigned to riders one-by-one. No notion of **batching** several deliveries for one rider or **ordering** stops (route).

**What we did:**  
- **Batching:** A batch of PENDING deliveries can be assigned to a single rider; they share a `batch_id`.  
- **Route order:** When a rider fetches their assigned deliveries, the list is **sorted by distance** from the rider’s current position to each delivery’s destination (simple “nearest first” route).

### Flow

```
Merchant / ops:
    - POST .../delivery/assign-batch
    - Body: { "deliveryOrderPublicIds": ["id1","id2",...], "riderPublicId": "rider-id" }
    - All those deliveries get the same batch_id (UUID) and the same rider; status → ASSIGNED.

Rider app:
    - GET assigned deliveries for rider
    - Backend returns them sorted by distance (rider’s current lat/lng → delivery’s delivery_lat/lng).
    - Rider uses that order as the suggested route (first = nearest, etc.).
```

### Key pieces

| What | Where |
|------|--------|
| Assign many deliveries to one rider (batch) | `LogisticsService.assignBatch(storeId, deliveryOrderPublicIds, riderPublicId)` |
| REST for assign batch | `POST /stores/{storePublicId}/delivery/assign-batch` (body: `AssignBatchRequest`) |
| Route order (nearest first) | `RiderDeliveryService.getAssignedDeliveries()` – sorts by `GeoUtils.distanceKm(riderLat, riderLng, deliveryLat, deliveryLng)` |
| Batch id on delivery | `DeliveryOrder.batchId` (already existed; now set when using assign-batch) |

---

## 4. Logistics partner integration

**Problem before:** Delivery was in-house only (create DeliveryOrder, assign rider, rider app updates status). No way to use **external carriers** (e.g. Aramex) or to receive **status updates from them**.

**What we did:**  
- **Carrier-agnostic interface** – `LogisticsPartner`: createShipment, getStatus, cancelShipment.  
- **DeliveryOrder** can represent an external shipment: `carrier_code`, `external_shipment_id`, `tracking_url`.  
- **Internal** carrier: existing flow (no external API).  
- **Aramex** adapter: implements `LogisticsPartner`, calls Aramex APIs (create/track/cancel), configurable (enabled, base URL, credentials).  
- **Carrier webhook:** External partners can push status updates; we map to our status and publish `DeliveryStatusChangedEvent`.

### Flow

```
Using an external carrier (e.g. Aramex):
    1. You create a DeliveryOrder (or have one from payment success).
    2. Call AramexLogisticsPartner.createShipment(ShipmentRequest) with pickup/delivery/parcel info.
    3. Store on DeliveryOrder: carrier_code = "ARAMEX", external_shipment_id = result.externalShipmentId, tracking_url = result.trackingUrl.
    4. Status updates can come from:
       - Your own polling: getStatus(externalShipmentId) and update DeliveryOrder.
       - Partner webhook: POST /webhooks/carrier/ARAMEX with { "externalShipmentId": "...", "status": "DELIVERED" }.
    5. CarrierWebhookService finds DeliveryOrder by carrier_code + external_shipment_id, maps status, updates entity, publishes DeliveryStatusChangedEvent (same event as rider app updates).
```

### Key pieces

| What | Where |
|------|--------|
| Interface for any carrier | `LogisticsPartner` – getCarrierCode(), createShipment(), getStatus(), cancelShipment() |
| In-house carrier | `InternalLogisticsPartner` – getStatus/cancel by delivery publicId; createShipment is no-op (you use LogisticsService.createDeliveryOrder) |
| Aramex | `AramexLogisticsPartner` (+ `AramexShipmentRequest`, `AramexXmlBuilder`, `AramexResponseParser`) – only loaded if `app.logistics.aramex.enabled=true` |
| Carrier fields on delivery | `DeliveryOrder.carrierCode`, `externalShipmentId`, `trackingUrl` (migration V49) |
| Webhook: partner → our status | `POST /webhooks/carrier/{carrierCode}` body `CarrierWebhookPayload` (externalShipmentId, status, optional trackingUrl) |
| Map partner status → DeliveryStatus | `CarrierWebhookService.handleStatusUpdate()` – maps e.g. PICKUP → PICKED_UP, DELIVERY → DELIVERED, then updates DeliveryOrder and publishes `DeliveryStatusChangedEvent` |
| Find delivery by carrier + external id | `DeliveryOrderRepository.findByCarrierCodeAndExternalShipmentId()` |

### Config (Aramex)

- **application.yml:** `app.logistics.aramex` – enabled, base-url, username, password, account-number, account-entity, account-country-code.  
- **.env.example:** `APP_LOGISTICS_ARAMEX_*` variables.

---

## Quick reference: new/important files

| Area | Files (main) |
|------|-------------------------------|
| **1. Reservation** | `InventoryService` (reserve/release/convert by reference, reserveVariantQuantity, getAllocationCandidatesForVariant), `CheckoutService`, `PaymentService`, `PaystackWebhookService`, `ReservationExpiryJob`, `Order.reservationExpiresAt`, migrations V46 |
| **2. Order splitting** | `OrderItemAllocation`, `Shipment`, `ShipmentRepository`, `OrderItemAllocationRepository`, `CheckoutService` (allocation + reserve per location), `PaymentService` (one delivery per shipment), `DeliveryOrder.shipmentId`, migrations V47, V48 |
| **3. Routing** | `LogisticsService.assignBatch()`, `RiderDeliveryService.getAssignedDeliveries()` (sort by distance), `AssignBatchRequest`, `POST .../delivery/assign-batch` |
| **4. Partners** | `LogisticsPartner`, `InternalLogisticsPartner`, `AramexLogisticsPartner` (+ aramex package), `CarrierWebhookService`, `CarrierWebhookController`, `CarrierWebhookPayload`, `DeliveryOrder` carrier fields, migration V49 |

---

## Summary table

| Challenge | Before | After |
|-----------|--------|--------|
| **1. Reservation** | Deduct on order create; payment failure = stock already gone | Reserve on checkout; convert to sale on payment success; release on failure/expiry/cancel |
| **2. Order splitting** | One order → one delivery; no record of which location fulfilled what | Allocations (line → location → qty); Shipments (one per order+location); one DeliveryOrder per Shipment |
| **3. Delivery routing** | Single delivery assign, no batch or route order | assignBatch(); getAssignedDeliveries() sorted by distance (route order) |
| **4. Logistics partner** | In-house only | LogisticsPartner interface; Internal + Aramex adapters; carrier_code/external_shipment_id/tracking_url; carrier webhook → DeliveryStatusChangedEvent |

If you want more detail on a specific class or API (e.g. exact method names or request bodies), say which part and we can add a short “API/Code” subsection there.
