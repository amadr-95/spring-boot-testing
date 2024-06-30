package testing.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJpaTest //to test jpa queries
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository underTest;

    @Test
    void itShouldSelectCustomerByPhoneNumber() {
        //Given
        String phoneNumber = "1234567";

        //When
        underTest.save(new Customer(UUID.randomUUID(), "Amador", phoneNumber));

        //Then
        assertThat(underTest.existsCustomerByPhoneNumber(phoneNumber)).isTrue();
        assertThat(underTest.findCustomerByPhoneNumberNative(phoneNumber)).isPresent();
        assertThat(underTest.findCustomerByPhoneNumber(phoneNumber)).isPresent();
    }

    @Test
    void itShouldNotSelectCustomerByPhoneNumberDoesNotExist() {
        //Given
        String phoneNumber = "000";

        //When

        //Then
        assertThat(underTest.existsCustomerByPhoneNumber(phoneNumber)).isFalse();
        assertThat(underTest.findCustomerByPhoneNumberNative(phoneNumber)).isNotPresent();
        assertThat(underTest.findCustomerByPhoneNumber(phoneNumber)).isNotPresent();
    }


    @Test
    void itShouldSaveCustomer() {
        //Given
        Customer customer = new Customer("Amador", "1234567");

        //When
        underTest.save(customer);

        //Then
        List<Customer> customers = underTest.findAll();
        assertThat(customers.size()).isEqualTo(1);

        Customer c = customers.get(0);

        assertThat(c).usingRecursiveComparison()
                .ignoringFields("customerId")
                .isEqualTo(customer);
    }

    @Test
    void itShouldNotSaveCustomerWhenNameIsNull() {
        //Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, null, "1234567");

        //When

        //Then
        assertThatThrownBy(() -> underTest.save(customer))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(
                        "not-null property references a null or transient value : testing.customer.Customer.name");
    }

    @Test
    void itShouldNotSaveCustomerWhenPhoneNumberIsNull() {
        //Given
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "Amador", null);

        //When

        //Then
        assertThatThrownBy(() -> underTest.save(customer))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(
                        "not-null property references a null or transient value : testing.customer.Customer.phoneNumber");
    }
}