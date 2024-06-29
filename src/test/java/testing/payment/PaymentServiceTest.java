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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

        assertThat(paymentArgumentCaptorValue).isNotNull();
        assertThat(paymentArgumentCaptorValue.getPaymentDescription()).isEqualTo(paymentDescription);
        assertThat(paymentArgumentCaptorValue.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(paymentArgumentCaptorValue.getAmount()).isEqualTo(amount);
        assertThat(paymentArgumentCaptorValue.getCurrency()).isEqualTo(currency);
        assertThat(paymentArgumentCaptorValue.getCustomerId()).isEqualTo(customerId);

    }

    @Test
    void itShouldNotSavePaymentWhenCustomerIdDoesNotExist() {
        //Given
        String paymentDescription = "description";
        String paymentMethod = "card";
        BigDecimal amount = new BigDecimal(10);

        UUID customerId = UUID.randomUUID();

        given(customerRepository.findById(customerId))
                .willReturn(Optional.empty());

        PaymentRequest request = PaymentRequest.builder()
                .paymentMethod(paymentMethod)
                .paymentDescription(paymentDescription)
                .amount(amount)
                .currency(EUR)
                .build();

        //When
        //Then
        assertThatThrownBy(() -> underTest.chargePayment(customerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Customer [%s] does not exist", customerId));

        then(paymentRepository).should(never()).save(any());
        then(paymentRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void itShouldThrowExceptionWhenCurrencyIsNotSupported() {
        //Given
        UUID customerId = UUID.randomUUID();
        String paymentDescription = "description";
        String paymentMethod = "card";
        BigDecimal amount = new BigDecimal(10);

        // customer exists
        given(customerRepository.findById(customerId)).
                willReturn(Optional.of(mock(Customer.class)));

        // payment request
        PaymentRequest request = PaymentRequest.builder()
                .paymentMethod(paymentMethod)
                .paymentDescription(paymentDescription)
                .amount(amount)
                .currency(GBP) //not accepted
                .build();


        //When
        //Then
        assertThatThrownBy(() -> underTest.chargePayment(customerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Currency [%s] not supported", request.getCurrency()));

        then(cardPaymentCharger).shouldHaveNoMoreInteractions();
        then(paymentRepository).should(never()).save(any(Payment.class));
    }

    @Test
    void itShouldThrowExceptionWhenCardIsNotDebited() {
        //Given
        UUID customerId = UUID.randomUUID();
        String paymentDescription = "description";
        String paymentMethod = "card";
        BigDecimal amount = new BigDecimal(10);
        Currency currency = EUR;

        // customet exists
        given(customerRepository.findById(customerId))
                .willReturn(Optional.of(mock(Customer.class)));

        // payment request
        PaymentRequest request = PaymentRequest.builder()
                .paymentMethod(paymentMethod)
                .paymentDescription(paymentDescription)
                .amount(amount)
                .currency(currency)
                .build();

        // card charge fail
        given(cardPaymentCharger.chargeCard(
                paymentMethod,
                amount,
                currency,
                paymentDescription
        )).willReturn(new CardPaymentCharge(false));


        //When
        //Then
        assertThatThrownBy(() -> underTest.chargePayment(customerId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Card not debited for customer [%s]", customerId));

        then(paymentRepository).should(never()).save(any(Payment.class));
    }
}