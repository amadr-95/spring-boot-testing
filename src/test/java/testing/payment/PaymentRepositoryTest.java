package testing.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static testing.payment.Currency.*;

@DataJpaTest
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository underTest;

    @Test
    void itShouldSavePayment() {
        //Given
        long paymentId = 1L;
        Payment payment = new Payment(paymentId,
                "card",
                "donation",
                new BigDecimal(10),
                EUR,
                UUID.randomUUID());

        //When
        underTest.save(payment);

        //Then
        Optional<Payment> optionalPayment = underTest.findById(paymentId);
        assertThat(optionalPayment)
                .isPresent()
                .hasValueSatisfying(
                        p -> assertThat(p).usingRecursiveComparison().isEqualTo(payment)
                );

    }
}