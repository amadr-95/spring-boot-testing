package testing.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsCustomerByPhoneNumber(String phoneNumber);

    @Query(
            value = "SELECT * FROM Customer WHERE phone_number = :phone_number",
            nativeQuery = true
    )
    Optional<Customer> findCustomerByPhoneNumberNative(@Param("phone_number") String phoneNumber);


    @Query(
            value = "SELECT c FROM Customer c WHERE c.phoneNumber = ?1"
    )
    Optional<Customer> findCustomerByPhoneNumber(String phoneNumber);

}
