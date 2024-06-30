package testing.payment.stripe;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import testing.payment.CardPaymentCharge;
import testing.payment.CardPaymentCharger;
import testing.payment.Currency;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(
        value = "stripe.mocked",
        havingValue = "true"
)
public class MockStripeService implements CardPaymentCharger {
    @Override
    public CardPaymentCharge chargeCard(String method, BigDecimal amount, Currency currency, String description) {
        return CardPaymentCharge.builder()
                .isCardDebited(true)
                .build();
    }
}
