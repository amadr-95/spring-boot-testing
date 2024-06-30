package testing.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{customerId}")
    public void makePayment(@PathVariable UUID customerId, @RequestBody PaymentRequest paymentRequest) {
        paymentService.chargePayment(customerId, paymentRequest);
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }
}
