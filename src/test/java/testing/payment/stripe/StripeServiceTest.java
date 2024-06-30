package testing.payment.stripe;

import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import testing.payment.CardPaymentCharge;
import testing.payment.Currency;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static testing.payment.Currency.EUR;

@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

    @Captor
    private ArgumentCaptor<PaymentIntentCreateParams> paramsArgumentCaptor;

    @Captor
    private ArgumentCaptor<RequestOptions> requestOptionsArgumentCaptor;

    private StripeService underTest;

    @BeforeEach
    public void setUp() {
        underTest = new StripeService();
    }

    @Test
    void itShouldChargeCard() {
        //Given
        String method = "method";
        String description = "description";
        BigDecimal amount = new BigDecimal(10);
        Currency currency = EUR;

        // Mock the PaymentIntent and Charge
        PaymentIntent paymentIntentMock = mock(PaymentIntent.class);
        Charge chargeMock = mock(Charge.class);

        // Set up the behavior for the mock objects
        given(paymentIntentMock.getLatestChargeObject()).willReturn(chargeMock);
        given(chargeMock.getPaid()).willReturn(true);


        try (MockedStatic<PaymentIntent> paymentIntentMockedStatic =
                     Mockito.mockStatic(PaymentIntent.class)) {

            //Mock the static method call
            paymentIntentMockedStatic.when(() -> PaymentIntent.create(
                            any(PaymentIntentCreateParams.class),
                            any(RequestOptions.class)
                    )
            ).thenReturn(paymentIntentMock);

            //When
            CardPaymentCharge cardPaymentCharge =
                    underTest.chargeCard(method, amount, currency, description);

            //Then
            assertThat(cardPaymentCharge).isNotNull();
            assertThat(cardPaymentCharge.isCardDebited()).isTrue();

            // .. capture the arguments passed to the static method
            paymentIntentMockedStatic.verify(() -> PaymentIntent.create(
                    paramsArgumentCaptor.capture(),
                    requestOptionsArgumentCaptor.capture()
            ));

            //validate params
            PaymentIntentCreateParams paramsArgumentCaptorValue = paramsArgumentCaptor.getValue();
            assertThat(paramsArgumentCaptorValue.getAmount()).isEqualTo(amount.longValue());
            assertThat(paramsArgumentCaptorValue.getCurrency()).isEqualTo(currency.name().toLowerCase());
            assertThat(paramsArgumentCaptorValue.getDescription()).isEqualTo(description);
            assertThat(paramsArgumentCaptorValue.getPaymentMethod()).isEqualTo(method);

            //validate request options
            RequestOptions requestOptionsArgumentCaptorValue = requestOptionsArgumentCaptor.getValue();
            assertThat(requestOptionsArgumentCaptorValue.getApiKey()).isEqualTo("sk_test_CGGvfNiIPwLXiDwaOfZ3oX6Y");
        }
    }


}