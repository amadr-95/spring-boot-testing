package testing.payment.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import testing.payment.CardPaymentCharge;
import testing.payment.CardPaymentCharger;
import testing.payment.Currency;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(
        value = "stripe.mocked",
        havingValue = "false"
)
public class StripeService implements CardPaymentCharger {

    private final RequestOptions requestOptions = RequestOptions.builder()
            .setApiKey("sk_test_CGGvfNiIPwLXiDwaOfZ3oX6Y")
            .build();

    @Override
    public CardPaymentCharge chargeCard(String method, BigDecimal amount, Currency currency, String description) {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amount.longValue())
                        .setCurrency(currency.name().toLowerCase()) // "usd", "eur", ...
                        .setDescription(description)
                        .setPaymentMethod(method)
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .build();
        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params, requestOptions);
            Charge charge = paymentIntent.getLatestChargeObject();
            return new CardPaymentCharge(charge.getPaid());
        } catch (StripeException e) {
            throw new IllegalStateException("Can not make Stripe charge", e);
        }
    }
}
