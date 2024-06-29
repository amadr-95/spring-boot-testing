package testing.payment.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import testing.payment.CardPaymentCharge;
import testing.payment.CardPaymentCharger;
import testing.payment.Currency;

import java.math.BigDecimal;

@Service
@Primary
public class StripeService implements CardPaymentCharger {

    private final RequestOptions requestOptions = RequestOptions.builder()
            .setStripeAccount("acct_1032D82eZvKYlo2C")
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
            return new CardPaymentCharge(paymentIntent.getLatestChargeObject().getPaid());
        } catch (StripeException e) {
            throw new IllegalStateException(e);
        }
    }
}
