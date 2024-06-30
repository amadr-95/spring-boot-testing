package testing.customer;

import com.google.i18n.phonenumbers.NumberParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import testing.utils.PhoneNumberValidator;

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

    @Mock
    private PhoneNumberValidator phoneNumberValidator;

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
    void itShouldSaveCustomer() throws NumberParseException {
        //Given
        String phoneNumber = "600000000";
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Amador", phoneNumber
        );

        // ... no customer with that phone number
        given(customerRepository.findCustomerByPhoneNumberNative(phoneNumber))
                .willReturn(Optional.empty());

        // given a valid number (mock)
        given(phoneNumberValidator.validate(phoneNumber)).willReturn(true);

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
    void itShouldNotSaveCustomerWhenAlreadyExists() throws NumberParseException {
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

        // given a valid number (mock)
        given(phoneNumberValidator.validate(phoneNumber)).willReturn(true);

        //When
        underTest.registerNewCustomer(request);

        //Then
        then(customerRepository).shouldHaveNoMoreInteractions();
//        then(customerRepository).should(never()).save(any());
    }

    @Test
    void itShouldThrowExceptionWhenPhoneNumberIsTaken() throws NumberParseException {
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

        // given a valid number (mock)
        given(phoneNumberValidator.validate(phoneNumber)).willReturn(true);

        //When
        //Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Phone number [%s] is already taken", phoneNumber));
        then(customerRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void itShouldThrowExceptionWhenPhoneNumberIsNotValid() throws NumberParseException {
        //Given
        String phoneNumber = "1234567";
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Amador", phoneNumber
        );

        // given a invalid number (mock)
        given(phoneNumberValidator.validate(phoneNumber)).willReturn(false);

        //When
        //Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(String.format("Phone number [%s] is not valid", request.getPhoneNumber()));

        then(customerRepository).shouldHaveNoMoreInteractions();
    }
}