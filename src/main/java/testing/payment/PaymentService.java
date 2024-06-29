package testing.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import testing.customer.CustomerRepository;

import java.util.Set;
import java.util.UUID;

import static testing.payment.Currency.EUR;
import static testing.payment.Currency.USD;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final CardPaymentCharger cardPaymentCharger;
    private final Set<Currency> SUPPORTED_CURRENCIES = Set.of(USD, EUR);


    public void chargePayment(UUID customerId, PaymentRequest paymentRequest) {
        //check if customer exists
        if (customerRepository.findById(customerId).isEmpty())
            throw new IllegalArgumentException(
                    String.format("Customer [%s] does not exist", customerId));

        //check if currency is supported
        if (!SUPPORTED_CURRENCIES.contains(paymentRequest.getCurrency()))
            throw new IllegalArgumentException(
                    String.format("Currency [%s] not supported", paymentRequest.getCurrency()));

        CardPaymentCharge cardPaymentCharge = cardPaymentCharger.chargeCard(
                paymentRequest.getPaymentMethod(),
                paymentRequest.getAmount(),
                paymentRequest.getCurrency(),
                paymentRequest.getPaymentDescription()
        );

        if(!cardPaymentCharge.isCardDebited())
            throw new IllegalArgumentException(
                    String.format("Card not debited for customer [%s]", customerId));

        Payment payment = Payment.builder()
                .paymentMethod(paymentRequest.getPaymentMethod())
                .amount(paymentRequest.getAmount())
                .currency(paymentRequest.getCurrency())
                .paymentDescription(paymentRequest.getPaymentDescription())
                .customerId(customerId)
                .build();

        paymentRepository.save(payment);

    }
}
