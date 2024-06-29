package testing.payment;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import testing.customer.Customer;
import testing.customer.CustomerRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static testing.payment.Currency.*;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Captor
    private ArgumentCaptor<Payment> paymentArgumentCaptor;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CardPaymentCharger cardPaymentCharger;

    @InjectMocks
    private PaymentService underTest;

    @Test
    void itShouldChargePaymentSuccesfully() {
        //Given
        UUID customerId = UUID.randomUUID();
        String paymentDescription = "description";
        String paymentMethod = "card";
        BigDecimal amount = new BigDecimal(10);
        Currency currency = EUR;

        // ... customer exists
        given(customerRepository.findById(customerId))
                .willReturn(Optional.ofNullable(mock(Customer.class)));

        // .. payment request
        PaymentRequest request = new PaymentRequest(
                paymentMethod,
                paymentDescription,
                amount,
                currency
        );

        // .. card charged succesfully
        given(cardPaymentCharger.chargeCard(
                request.getPaymentMethod(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentDescription()
        )).willReturn(new CardPaymentCharge(true));

        //When
        underTest.chargePayment(customerId, request);

        //Then
        then(paymentRepository).should().save(paymentArgumentCaptor.capture());

        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue();

        /*assertThat(paymentArgumentCaptorValue)
                .usingRecursiveComparison()
                .ignoringFields("customerId", "paymentId")
                .isEqualTo(request);*/

        assertThat(paymentArgumentCaptorValue).isNotNull();
        assertThat(paymentArgumentCaptorValue.getPaymentDescription()).isEqualTo(paymentDescription);
        assertThat(paymentArgumentCaptorValue.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(paymentArgumentCaptorValue.getAmount()).isEqualTo(amount);
        assertThat(paymentArgumentCaptorValue.getCurrency()).isEqualTo(currency);
        assertThat(paymentArgumentCaptorValue.getCustomerId()).isEqualTo(customerId);

    }
}