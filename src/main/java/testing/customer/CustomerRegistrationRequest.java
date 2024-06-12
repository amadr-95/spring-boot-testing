package testing.customer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class CustomerRegistrationRequest {
    private String name;
    private String phoneNumber;
}
