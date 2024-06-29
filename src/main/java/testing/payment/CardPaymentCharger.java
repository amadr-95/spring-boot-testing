package testing.payment;

import java.math.BigDecimal;

public interface CardPaymentCharger {

    CardPaymentCharge chargeCard (
            String method,
            BigDecimal amount,
            Currency currency,
            String description
    );
}
