package testing.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberValidatorTest {

    private PhoneNumberValidator underTest;

    @BeforeEach
    public void setUp() {
        underTest = new PhoneNumberValidator();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "+34660000000,true",
            "660000000,true",
            "34660000000,true",
            "+3466000000000,false",
            "+366000000000,false"
    })
    void itShouldValidatePhoneNumber(String phoneNumber, boolean expected) throws NumberParseException {
        //When
        boolean isValid = underTest.validate(phoneNumber);
        //Then
        assertThat(isValid).isEqualTo(expected);
    }
}
