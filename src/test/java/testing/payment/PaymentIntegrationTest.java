package testing.payment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import testing.customer.Customer;
import testing.customer.CustomerRegistrationRequest;
import testing.customer.CustomerRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static testing.payment.Currency.EUR;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    /*@Autowired
    private PaymentRepository paymentRepository;*/

    @Autowired
    private MockMvc mockMvc; //MockMVC -> test RESTful API

    @Test
    void itShouldCreatePaymentSuccessfully() throws Exception {
        //Given

        // .. a customer request
        String phoneNumber = "600000000";
        CustomerRegistrationRequest customerRequest = CustomerRegistrationRequest.builder()
                .name("Amador")
                .phoneNumber(phoneNumber)
                .build();

        // a payment request
        String method = "method";
        String description = "description";
        BigDecimal amount = new BigDecimal(10);
        Currency currency = EUR;
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .paymentMethod(method)
                .paymentDescription(description)
                .amount(amount)
                .currency(currency)
                .build();

        //When
        ResultActions resultCustomerRequestActions = mockMvc.perform(post("/api/v1/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(customerRequest)) //json object
        );

        // get the customerId after insertion in db (client does not send any id)
        Optional<Customer> optionalCustomer = customerRepository.findCustomerByPhoneNumberNative(phoneNumber);
        UUID customerId = null;
        if (optionalCustomer.isPresent())
            customerId = optionalCustomer.get().getId();


        ResultActions resultPaymentActions = mockMvc.perform(post("/api/v1/payment/{customerId}", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(paymentRequest))
        );

        //Then
        resultCustomerRequestActions.andExpect(status().isOk());
        resultPaymentActions.andExpect(status().isOk());

        //assertions using PaymentRepository
        
        /*assertThat(paymentRepository.findAll().size()).isEqualTo(1);
        Payment payment = paymentRepository.findAll().get(0);
        assertThat(payment.getPaymentDescription()).isEqualTo(description);
        assertThat(payment.getPaymentMethod()).isEqualTo(method);
        assertThat(payment.getAmount().longValue()).isEqualTo(amount.longValue());
        assertThat(payment.getCurrency()).isEqualTo(currency);
        assertThat(payment.getCustomerId()).isEqualTo(customerId);*/

        // assertions using get endpoint
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/payment")).andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();

        List<Payment> payments = new ObjectMapper().readValue(jsonResponse, new TypeReference<>() {});

        assertThat(payments.size()).isEqualTo(1);
        //get the payment
        Payment payment = payments.get(0);

        assertThat(payment.getPaymentDescription()).isEqualTo(description);
        assertThat(payment.getPaymentMethod()).isEqualTo(method);
        assertThat(payment.getAmount().longValue()).isEqualTo(amount.longValue());
        assertThat(payment.getCurrency()).isEqualTo(currency);
        assertThat(payment.getCustomerId()).isEqualTo(customerId);

    }
}
