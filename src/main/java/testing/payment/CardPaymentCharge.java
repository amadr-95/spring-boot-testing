package testing.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CardPaymentCharge {
    private boolean isCardDebited;
}
