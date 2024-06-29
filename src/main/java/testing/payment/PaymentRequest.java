package testing.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
public class PaymentRequest {
    private String paymentMethod;
    private String paymentDescription;
    private BigDecimal amount;
    private Currency currency;
}
