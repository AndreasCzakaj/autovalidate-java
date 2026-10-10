package eu.binarystars.autovalidate;

import org.junit.jupiter.api.Test;

import jakarta.validation.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class DemoAutoValidateConstructorTest {
    @Test
    public void test_demo() {
        // DemoBean defines validation rules for its validity.
        // Let's try it.
        DemoAutovalidateConstructor demo = DemoAutovalidateConstructor.builder()  // not done yet
                .firstName("Joey")                // not done yet
                .lastName("Ramone")               // not done yet
                .email("joey@ramones.com")        // not done yet
                .build();
        // If we are still alive at this point then the object MUST have been correct.

        // let's check again manually:
        Set<ConstraintViolation<DemoAutovalidateConstructor>> violations = validate(demo);
        assertEquals(0, violations.size(), "Object should be valid.");

        // Fortunately, making it INVALID again is no possible...
        // let's "clone" it instead:
        try {
            final DemoAutovalidateConstructor demo2 = demo.toBuilder()
                    .email("joey_at_ramones.com")
                    .build();
            fail("This should have failed because the object is not valid");
        }
        catch (ConstraintViolationException e) {
            final Set<ConstraintViolation<?>> violations2 = e.getConstraintViolations();
            assertEquals(1, violations2.size(), "Object should have 1 error because the email address is wrong.");
        }

        // At not point in the process has the object been available in an invalid state.
        // I.e. it can be trusted anywhere it is used.
    }

    private Set<ConstraintViolation<DemoAutovalidateConstructor>> validate(final DemoAutovalidateConstructor demo) {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final Validator validator = validatorFactory.getValidator();

        return validator.validate(demo);
    }
}
