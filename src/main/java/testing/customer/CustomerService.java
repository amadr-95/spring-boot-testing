package testing.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public void registerNewCustomer(CustomerRegistrationRequest request) {
        //check if phoneNumber already exists
        if (customerRepository.existsCustomerByPhoneNumber(request.getPhoneNumber()))
            throw new IllegalArgumentException("Phone number already exists");

        Customer customer = Customer.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        customerRepository.save(customer);

    }

}
