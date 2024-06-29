package testing.payment;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CardPaymentChargerImp implements CardPaymentCharger {

    @Override
    public CardPaymentCharge chargeCard(String method, BigDecimal amount, Currency currency, String description) {
        return CardPaymentCharge.builder()
                .isCardDebited(true)
                .build();
    }
}
