package com.securemarts.domain.payment.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.logistics.dto.CreateDeliveryOrderRequest;
import com.securemarts.domain.logistics.service.LogisticsService;
import com.securemarts.domain.order.entity.Order;
import com.securemarts.domain.payment.dto.InitiatePaymentRequest;
import com.securemarts.domain.payment.dto.PaymentResponse;
import com.securemarts.domain.payment.entity.PaymentTransaction;
import com.securemarts.domain.payment.gateway.FlutterwaveGateway;
import com.securemarts.domain.payment.gateway.PaystackGateway;
import com.securemarts.domain.payment.gateway.PaymentGateway;
import com.securemarts.domain.order.repository.OrderRepository;
import com.securemarts.domain.payment.repository.PaymentTransactionRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final StoreRepository storeRepository;
    private final OrderRepository orderRepository;
    private final LogisticsService logisticsService;
    private final List<PaymentGateway> gateways;
    private Map<String, PaymentGateway> gatewayByName;

    @jakarta.annotation.PostConstruct
    void init() {
        gatewayByName = gateways.stream().collect(Collectors.toMap(PaymentGateway::name, Function.identity()));
    }

    @Transactional
    public PaymentResponse initiate(Long storeId, InitiatePaymentRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", String.valueOf(storeId));
        }
        String ref = "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        PaymentGateway gateway = gatewayByName.get(request.getGateway() != null ? request.getGateway().toUpperCase() : "PAYSTACK");
        if (gateway == null) gateway = gatewayByName.get("PAYSTACK");
        if (gateway == null) throw new BusinessRuleException("No payment gateway configured");

        PaymentGateway.InitResult result = gateway.initiate(PaymentGateway.InitRequest.builder()
                .email(request.getEmail())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "NGN")
                .reference(ref)
                .callbackUrl(request.getCallbackUrl())
                .build());

        PaymentTransaction txn = new PaymentTransaction();
        txn.setStoreId(storeId);
        txn.setOrderId(resolveOrderId(request.getOrderId()));
        txn.setAmount(request.getAmount());
        txn.setCurrency(request.getCurrency() != null ? request.getCurrency() : "NGN");
        txn.setStatus(result.isSuccess() ? PaymentTransaction.PaymentStatus.INITIATED : PaymentTransaction.PaymentStatus.PENDING);
        txn.setGateway(gateway.name());
        txn.setGatewayReference(result.getGatewayReference());
        txn.setGatewayResponse(result.getMessage());
        txn = paymentTransactionRepository.save(txn);

        String orderPublicId = txn.getOrderId() != null
                ? orderRepository.findById(txn.getOrderId()).map(o -> o.getPublicId()).orElse(null)
                : null;
        return PaymentResponse.from(txn, result.getAuthorizationUrl(), orderPublicId);
    }

    @Transactional
    public PaymentResponse verify(Long storeId, String paymentPublicId) {
        PaymentTransaction txn = paymentTransactionRepository.findByPublicId(paymentPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentPublicId));
        if (!txn.getStoreId().equals(storeId)) throw new ResourceNotFoundException("Payment", paymentPublicId);
        String orderPublicId = txn.getOrderId() != null
                ? orderRepository.findById(txn.getOrderId()).map(o -> o.getPublicId()).orElse(null)
                : null;
        PaymentGateway gateway = gatewayByName.get(txn.getGateway());
        if (gateway == null) return PaymentResponse.from(txn, null, orderPublicId);
        PaymentGateway.VerifyResult vr = gateway.verify(txn.getGatewayReference());
        if (vr.isSuccess() && ("success".equalsIgnoreCase(vr.getStatus()) || "successful".equalsIgnoreCase(vr.getStatus()))) {
            txn.setStatus(PaymentTransaction.PaymentStatus.SUCCESS);
            if (txn.getOrderId() != null) {
                orderRepository.findById(txn.getOrderId()).ifPresent(order -> {
                    order.setStatus(Order.OrderStatus.PAID);
                    orderRepository.save(order);
                    if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isBlank()
                            && order.getDeliveryLat() != null && order.getDeliveryLng() != null) {
                        CreateDeliveryOrderRequest deliveryRequest = new CreateDeliveryOrderRequest();
                        deliveryRequest.setOrderPublicId(order.getPublicId());
                        deliveryRequest.setDeliveryAddress(order.getDeliveryAddress());
                        deliveryRequest.setDeliveryLat(order.getDeliveryLat());
                        deliveryRequest.setDeliveryLng(order.getDeliveryLng());
                        deliveryRequest.setAutoAssign(true);
                        try {
                            logisticsService.createDeliveryOrder(order.getStoreId(), deliveryRequest);
                        } catch (Exception e) {
                            // Log but do not fail verify; merchant can create delivery manually
                            org.slf4j.LoggerFactory.getLogger(PaymentService.class).warn("Auto-create delivery on payment success failed: {}", e.getMessage());
                        }
                    }
                });
            }
        } else if (!vr.isSuccess()) {
            txn.setStatus(PaymentTransaction.PaymentStatus.FAILED);
        }
        txn.setGatewayResponse(vr.getMessage());
        paymentTransactionRepository.save(txn);
        return PaymentResponse.from(txn, null, orderPublicId);
    }

    private Long resolveOrderId(String orderIdOrPublicId) {
        if (orderIdOrPublicId == null || orderIdOrPublicId.isBlank()) return null;
        return orderRepository.findByPublicId(orderIdOrPublicId)
                .map(com.securemarts.domain.order.entity.Order::getId)
                .orElse(null);
    }
}
