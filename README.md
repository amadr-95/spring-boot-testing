# Spring Boot Testing

## Overview

This course has covered the following aspects:

- Unit Testing
- Integration Testing
- Testing External Services (Stripe)
- Mocking with Mockito
- Test Driven Development -`TDD`

## Architecture Diagram

- The diagram shows the different units tested in isolation as well as the integration test that covers the whole
  system.
  
  ![diagram](https://github.com/amadr-95/spring-boot-testing/assets/122611230/4c110060-b775-4789-a024-2595756376d5)


## Tests Coverage
  ![coverage](https://github.com/amadr-95/spring-boot-testing/assets/122611230/9be7967d-391f-4b78-8641-ec3b9b88bedc)  
  
  ![tests_passes](https://github.com/amadr-95/spring-boot-testing/assets/122611230/bf782de2-a340-4d03-ab2a-83b9a77ad8b5)

## Some test code

- Testing the _PaymentRepository_ class using the settings with `@DataJpaTest`.

```java
@DataJpaTest //to test JPA queries
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
```

- Testing the _CustomerService_ class using `@Mock` and `@Captor`.

```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PhoneNumberValidator phoneNumberValidator;

    @InjectMocks
    private CustomerService underTest;

    @Test
    void itShouldSaveCustomer() throws NumberParseException {
        //Given

        // .. phoneNumber
        String phoneNumber = "600000000";

        // .. a customer request
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Amador", phoneNumber
        );

        // given a valid number (mock)
        given(phoneNumberValidator.validate(phoneNumber)).willReturn(true);

        // ... no customer found with that phone number (mock)
        given(customerRepository.findCustomerByPhoneNumberNative(phoneNumber))
                .willReturn(Optional.empty());

        //When
        underTest.registerNewCustomer(request);

        //Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());

        Customer customer = customerArgumentCaptor.getValue();
        assertThat(customer).isNotNull();
        assertThat(customer.getName()).isEqualTo("Amador");
        assertThat(customer.getPhoneNumber()).isEqualTo(phoneNumber);
    }
}
```

- Testing the _StripeServiceTest_ class mocking static method with Mockito

```java
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
```

- Testing the _PaymentIntegrationTest_ class using `@SpringBootTest` and `@AutoConfigureMockMvc` that allows to make
  HTTP calls to Controllers.

```java
@SpringBootTest //It starts the application instead of testing separately
@AutoConfigureMockMvc //Needed to test endpoints
class PaymentIntegrationTest {

  @Autowired
  private CustomerRepository customerRepository;
  
  @Autowired
  private MockMvc mockMvc; //test RESTful API

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

        // assertions using get endpoint
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/payment")).andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();

        List<Payment> payments = new ObjectMapper().readValue(jsonResponse, new TypeReference<>() {
        });

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
```
