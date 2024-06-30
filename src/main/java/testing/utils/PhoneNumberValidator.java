package testing.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberValidator {

    public boolean validate(String phone) throws NumberParseException {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        PhoneNumber phoneNumberProto;
        if (phone.startsWith("+")) {
            phoneNumberProto = phoneUtil.parse(phone, null);
        } else {
            phoneNumberProto = phoneUtil.parse(phone, "ES");
        }

        return phoneUtil.isValidNumber(phoneNumberProto);

    }
}

