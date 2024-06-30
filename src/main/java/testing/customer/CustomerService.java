package testing.customer;

import com.google.i18n.phonenumbers.NumberParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import testing.utils.PhoneNumberValidator;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PhoneNumberValidator phoneValidator;

    public void registerNewCustomer(CustomerRegistrationRequest request) throws NumberParseException {
        if (request == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (!phoneValidator.validate(request.getPhoneNumber()))
            throw new IllegalArgumentException(String.format("Phone number [%s] is not valid", request.getPhoneNumber()));

        //check if phoneNumber already exists
        String phoneNumber = request.getPhoneNumber();

        Optional<Customer> optionalCustomer =
                customerRepository.findCustomerByPhoneNumberNative(phoneNumber);

        if (optionalCustomer.isPresent()) {
            //check if phone number belongs to the same customer comparing by name
            if (optionalCustomer.get().getName().equals(request.getName()))
                return;
            throw new IllegalArgumentException(String.format(
                    "Phone number [%s] is already taken", phoneNumber));
        }

        customerRepository.save(Customer.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build()
        );

    }

}
