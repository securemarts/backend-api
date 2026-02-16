package com.securemarts.domain.cart.dto;

import com.securemarts.domain.order.dto.OrderResponse;
import com.securemarts.domain.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order and payment created in one call; redirect customer to payment.authorizationUrl")
public class CreateOrderAndPayResponse {

    @Schema(description = "Created order (cart cleared)")
    private OrderResponse order;

    @Schema(description = "Initiated payment; use authorizationUrl to redirect to gateway")
    private PaymentResponse payment;
}
