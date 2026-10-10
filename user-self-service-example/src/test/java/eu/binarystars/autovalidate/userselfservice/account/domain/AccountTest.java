package eu.binarystars.autovalidate.userselfservice.account.domain;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    private static Account.AccountBuilder validNewAccount() {
        return Account.builder()
                .id(UUID.randomUUID())
                .username("joey")
                .passwordHash("some-hash")
                .email("joey@ramones.com")
                .termsAcceptedAt(Instant.now().minusSeconds(1))
                .status(AccountStatus.NEW)
                .emailVerificationToken("some-token")
                .emailVerificationTokenExpiresAt(Instant.now().plusSeconds(60));
    }

    @Test
    public void test_valid() {
        assertEquals(AccountStatus.NEW, validNewAccount().build().getStatus());
    }

    @Test
    public void test_empty() {
        final ConstraintViolationException e =
                assertThrows(ConstraintViolationException.class, () -> Account.builder().build());
        // id, username, passwordHash, email, termsAcceptedAt, status
        assertEquals(6, e.getConstraintViolations().size());
    }

    @Test
    public void test_invalidEmail() {
        final ConstraintViolationException e = assertThrows(ConstraintViolationException.class,
                () -> validNewAccount().email("joey_at_ramones.com").build());
        assertEquals(1, e.getConstraintViolations().size());
    }

    @Test
    public void test_newAccountNeedsToken() {
        assertThrows(ConstraintViolationException.class,
                () -> validNewAccount().emailVerificationToken(null).build());
    }

    @Test
    public void test_copyIsValidatedAsWell() {
        final Account account = validNewAccount().build();

        final Account verified = account.toBuilder()
                .status(AccountStatus.VERIFIED)
                .emailVerificationToken(null)
                .emailVerificationTokenExpiresAt(null)
                .build();
        assertNotSame(account, verified);
        assertEquals(account.getId(), verified.getId());
        assertEquals(AccountStatus.VERIFIED, verified.getStatus());
        assertEquals(AccountStatus.NEW, account.getStatus(), "The original must not have changed.");

        assertThrows(ConstraintViolationException.class, () -> account.toBuilder().status(null).build());
    }
}
