package testing.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest //to test jpa queries
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository underTest;

    @Test
    void itShouldSelectCustomerByPhoneNumber() {
        //Given
        //When
        //Then
    }

    @Test
    void itShouldReturnTrueIfCustomerExistsByPhoneNumber() {
        //Given
        //When
        //Then
    }

    @Test
    void itShouldSaveCustomer() {
        //Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Amador", "1234567");

        //When
        underTest.save(customer);

        //Then
        Optional<Customer> customerOptional = underTest.findById(id);
        assertThat(customerOptional).isPresent()
                .hasValueSatisfying(c -> {
                    /*assertThat(c.getId()).isEqualTo(id);
                    assertThat(c.getName()).isEqualTo("Amador");
                    assertThat(c.getPhoneNumber()).isEqualTo("1234567");*/
                    assertThat(c).usingRecursiveComparison().isEqualTo(customer);
                });
    }
}