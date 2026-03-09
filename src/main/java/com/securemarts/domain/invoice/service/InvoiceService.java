package com.securemarts.domain.invoice.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.repository.ProductVariantRepository;
import com.securemarts.domain.customer.entity.StoreCustomer;
import com.securemarts.domain.customer.repository.StoreCustomerRepository;
import com.securemarts.domain.customer.service.StoreCustomerService;
import com.securemarts.domain.invoice.dto.*;
import com.securemarts.domain.invoice.entity.Invoice;
import com.securemarts.domain.invoice.entity.InvoiceItem;
import com.securemarts.domain.invoice.entity.InvoicePayment;
import com.securemarts.domain.invoice.repository.InvoicePaymentRepository;
import com.securemarts.domain.invoice.repository.InvoiceRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final Pattern INVOICE_NUMBER_PATTERN = Pattern.compile("INV-(\\d+)");

    private final InvoiceRepository invoiceRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final StoreRepository storeRepository;
    private final StoreCustomerService storeCustomerService;
    private final StoreCustomerRepository storeCustomerRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public InvoiceResponse create(Long storeId, CreateInvoiceRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", String.valueOf(storeId));
        }
        StoreCustomer customer = storeCustomerService.getEntity(storeId, request.getStoreCustomerPublicId());
        String invoiceNumber = generateNextInvoiceNumber(storeId);
        Invoice inv = new Invoice();
        inv.setStoreId(storeId);
        inv.setStoreCustomerId(customer.getId());
        inv.setInvoiceNumber(invoiceNumber);
        inv.setStatus(Invoice.InvoiceStatus.DRAFT);
        inv.setCurrency("NGN");
        inv.setDueDate(request.getDueDate());
        inv.setNotes(request.getNotes());
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceItemRequest itemReq : request.getItems()) {
            validateItemRequest(itemReq);
            InvoiceItem item = toInvoiceItem(inv, itemReq);
            inv.getItems().add(item);
            total = total.add(item.getTotalPrice());
        }
        inv.setTotalAmount(total);
        inv = invoiceRepository.save(inv);
        return toResponse(inv, customer);
    }

    @Transactional
    public InvoiceResponse update(Long storeId, String invoicePublicId, UpdateInvoiceRequest request) {
        Invoice inv = invoiceRepository.findByStoreIdAndPublicId(storeId, invoicePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoicePublicId));
        if (inv.getStatus() != Invoice.InvoiceStatus.DRAFT) {
            throw new BusinessRuleException("Only draft invoices can be updated");
        }
        if (request.getDueDate() != null) inv.setDueDate(request.getDueDate());
        if (request.getNotes() != null) inv.setNotes(request.getNotes());
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            inv.getItems().clear();
            BigDecimal total = BigDecimal.ZERO;
            for (InvoiceItemRequest itemReq : request.getItems()) {
                validateItemRequest(itemReq);
                InvoiceItem item = toInvoiceItem(inv, itemReq);
                inv.getItems().add(item);
                total = total.add(item.getTotalPrice());
            }
            inv.setTotalAmount(total);
        }
        inv = invoiceRepository.save(inv);
        StoreCustomer cust = storeCustomerRepository.findById(inv.getStoreCustomerId()).orElseThrow();
        return toResponse(inv, cust);
    }

    private StoreCustomer getStoreCustomer(Long storeCustomerId) {
        return storeCustomerRepository.findById(storeCustomerId).orElseThrow();
    }

    /** Create an ISSUED invoice from a POS credit sale (used when syncing POS with paymentType=CREDIT). */
    @Transactional
    public Invoice createFromPosCredit(Long storeId, String storeCustomerPublicId, BigDecimal amount, String currency,
                                      List<PosCreditLineDto> lines) {
        StoreCustomer customer = storeCustomerService.getEntity(storeId, storeCustomerPublicId);
        String invoiceNumber = generateNextInvoiceNumber(storeId);
        Invoice inv = new Invoice();
        inv.setStoreId(storeId);
        inv.setStoreCustomerId(customer.getId());
        inv.setInvoiceNumber(invoiceNumber);
        inv.setStatus(Invoice.InvoiceStatus.ISSUED);
        inv.setTotalAmount(amount);
        inv.setCurrency(currency != null ? currency : "NGN");
        inv.setIssuedAt(Instant.now());
        for (PosCreditLineDto line : lines) {
            InvoiceItem item = new InvoiceItem();
            item.setInvoice(inv);
            item.setQuantity(line.getQuantity());
            item.setUnitPrice(line.getUnitPrice());
            item.setTotalPrice(line.getTotalPrice());
            if (line.getVariantPublicId() != null && !line.getVariantPublicId().isBlank()) {
                productVariantRepository.findByPublicId(line.getVariantPublicId()).ifPresent(v -> {
                    item.setProductVariant(v);
                    item.setDescription(v.getTitle());
                });
            }
            if (item.getDescription() == null) item.setDescription("Line item");
            inv.getItems().add(item);
        }
        return invoiceRepository.save(inv);
    }

    /** Resolve store customer public id to id for filtering. */
    public Long resolveCustomerId(Long storeId, String storeCustomerPublicId) {
        return storeCustomerRepository.findByStoreIdAndPublicId(storeId, storeCustomerPublicId)
                .map(StoreCustomer::getId)
                .orElseThrow(() -> new ResourceNotFoundException("StoreCustomer", storeCustomerPublicId));
    }

    @Transactional
    public InvoiceResponse issue(Long storeId, String invoicePublicId) {
        Invoice inv = invoiceRepository.findByStoreIdAndPublicId(storeId, invoicePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoicePublicId));
        if (inv.getStatus() != Invoice.InvoiceStatus.DRAFT) {
            throw new BusinessRuleException("Only draft invoices can be issued");
        }
        inv.setStatus(Invoice.InvoiceStatus.ISSUED);
        inv.setIssuedAt(Instant.now());
        inv = invoiceRepository.save(inv);
        StoreCustomer cust = getStoreCustomer(inv.getStoreCustomerId());
        return toResponse(inv, cust);
    }

    @Transactional
    public InvoiceResponse cancel(Long storeId, String invoicePublicId) {
        Invoice inv = invoiceRepository.findByStoreIdAndPublicId(storeId, invoicePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoicePublicId));
        if (inv.getStatus() != Invoice.InvoiceStatus.DRAFT && inv.getStatus() != Invoice.InvoiceStatus.ISSUED) {
            throw new BusinessRuleException("Only draft or issued invoices can be cancelled");
        }
        BigDecimal paid = invoicePaymentRepository.sumAmountByInvoiceId(inv.getId());
        if (paid != null && paid.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessRuleException("Cannot cancel invoice that has payments");
        }
        inv.setStatus(Invoice.InvoiceStatus.CANCELLED);
        inv = invoiceRepository.save(inv);
        StoreCustomer cust = getStoreCustomer(inv.getStoreCustomerId());
        return toResponse(inv, cust);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceSummaryResponse> list(Long storeId, Invoice.InvoiceStatus status, Long storeCustomerId,
                                            Instant from, Instant to, Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findByStoreIdAndFilters(storeId, status, storeCustomerId, from, to, pageable);
        return page.map(inv -> {
            StoreCustomer cust = getStoreCustomer(inv.getStoreCustomerId());
            return InvoiceSummaryResponse.builder()
                    .publicId(inv.getPublicId())
                    .invoiceNumber(inv.getInvoiceNumber())
                    .customerName(cust.getName())
                    .status(inv.getStatus())
                    .totalAmount(inv.getTotalAmount())
                    .dueDate(inv.getDueDate())
                    .issuedAt(inv.getIssuedAt())
                    .createdAt(inv.getCreatedAt())
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(Long storeId, String invoicePublicId) {
        Invoice inv = invoiceRepository.findByStoreIdAndPublicId(storeId, invoicePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoicePublicId));
        StoreCustomer cust = getStoreCustomer(inv.getStoreCustomerId());
        return toResponse(inv, cust);
    }

    private void validateItemRequest(InvoiceItemRequest itemReq) {
        if ((itemReq.getVariantPublicId() == null || itemReq.getVariantPublicId().isBlank())
                && (itemReq.getDescription() == null || itemReq.getDescription().isBlank())) {
            throw new BusinessRuleException("Each line item must have either variantPublicId or description");
        }
    }

    private InvoiceItem toInvoiceItem(Invoice inv, InvoiceItemRequest itemReq) {
        InvoiceItem item = new InvoiceItem();
        item.setInvoice(inv);
        item.setQuantity(itemReq.getQuantity());
        item.setUnitPrice(itemReq.getUnitPrice());
        item.setTotalPrice(itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        if (itemReq.getVariantPublicId() != null && !itemReq.getVariantPublicId().isBlank()) {
            productVariantRepository.findByPublicId(itemReq.getVariantPublicId()).ifPresent(item::setProductVariant);
            if (itemReq.getDescription() != null && !itemReq.getDescription().isBlank()) {
                item.setDescription(itemReq.getDescription());
            } else if (item.getProductVariant() != null) {
                item.setDescription(item.getProductVariant().getTitle());
            }
        } else {
            item.setDescription(itemReq.getDescription());
        }
        return item;
    }

    private InvoiceResponse toResponse(Invoice inv, StoreCustomer customer) {
        return InvoiceResponse.from(inv, customer.getName(), customer.getPublicId());
    }

    private String generateNextInvoiceNumber(Long storeId) {
        List<Invoice> last = invoiceRepository.findTopByStoreIdOrderByIdDesc(storeId, PageRequest.of(0, 1));
        int nextSeq = 1;
        if (!last.isEmpty()) {
            Matcher m = INVOICE_NUMBER_PATTERN.matcher(last.get(0).getInvoiceNumber());
            if (m.matches()) {
                nextSeq = Integer.parseInt(m.group(1)) + 1;
            }
        }
        return "INV-" + String.format("%05d", nextSeq);
    }

    @Transactional
    public InvoicePaymentResponse recordPayment(Long storeId, String invoicePublicId, RecordPaymentRequest request) {
        Invoice inv = invoiceRepository.findByStoreIdAndPublicId(storeId, invoicePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoicePublicId));
        if (inv.getStatus() == Invoice.InvoiceStatus.DRAFT || inv.getStatus() == Invoice.InvoiceStatus.CANCELLED || inv.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new BusinessRuleException("Cannot record payment for draft, cancelled, or fully paid invoice");
        }
        BigDecimal totalPaid = invoicePaymentRepository.sumAmountByInvoiceId(inv.getId());
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;
        BigDecimal afterPayment = totalPaid.add(request.getAmount());
        if (afterPayment.compareTo(inv.getTotalAmount()) > 0) {
            throw new BusinessRuleException("Payment amount would exceed invoice total");
        }
        InvoicePayment payment = new InvoicePayment();
        payment.setInvoice(inv);
        payment.setAmount(request.getAmount());
        payment.setCurrency(inv.getCurrency());
        payment.setPaymentMethod(InvoicePayment.PaymentMethod.valueOf(request.getPaymentMethod()));
        payment.setReference(request.getReference());
        payment.setPaidAt(request.getPaidAt() != null ? request.getPaidAt() : Instant.now());
        payment = invoicePaymentRepository.save(payment);
        inv.getPayments().add(payment);
        if (afterPayment.compareTo(inv.getTotalAmount()) >= 0) {
            inv.setStatus(Invoice.InvoiceStatus.PAID);
        } else {
            inv.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(inv);
        return InvoicePaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public List<InvoicePaymentResponse> listPayments(Long storeId, String invoicePublicId) {
        Invoice inv = invoiceRepository.findByStoreIdAndPublicId(storeId, invoicePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoicePublicId));
        return invoicePaymentRepository.findByInvoice_IdOrderByPaidAtAsc(inv.getId()).stream()
                .map(InvoicePaymentResponse::from)
                .toList();
    }
}
