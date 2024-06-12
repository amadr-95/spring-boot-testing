package testing.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService underTest;


    @Test
    void itShouldNotSaveCustomerWhenIsNull() {
        //Given
        //When

        //Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer cannot be null");
    }

    @Test
    void itShouldSaveCustomer() {
        //Given
        String phoneNumber = "1234567";
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Amador", phoneNumber
        );

        // ... no customer with that phone number
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

    @Test
    void itShouldNotSaveCustomerWhenAlreadyExists() {
        //Given
        String phoneNumber = "1234567";
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Amador", phoneNumber
        );

        // ... customer with that phone number exists and is the same
        given(customerRepository.findCustomerByPhoneNumberNative(phoneNumber))
                .willReturn(Optional.of(
                        Customer.builder()
                                .name(request.getName())
                                .phoneNumber(request.getPhoneNumber())
                                .build())
                );
        //When
        underTest.registerNewCustomer(request);

        //Then
        then(customerRepository).shouldHaveNoMoreInteractions();
//        then(customerRepository).should(never()).save(any());
    }

    @Test
    void itShouldThrowExceptionWhenPhoneNumberIsTaken() {
        //Given
        String phoneNumber = "1234567";
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Amador", phoneNumber
        );

        // ... customer with that phone number exists and is not the same
        given(customerRepository.findCustomerByPhoneNumberNative(phoneNumber))
                .willReturn(Optional.of(
                        Customer.builder()
                                .name("Sandra")
                                .phoneNumber(phoneNumber)
                                .build())
                );
        //When
        //Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Phone number [%s] is already taken", phoneNumber));
        then(customerRepository).shouldHaveNoMoreInteractions();
    }
}