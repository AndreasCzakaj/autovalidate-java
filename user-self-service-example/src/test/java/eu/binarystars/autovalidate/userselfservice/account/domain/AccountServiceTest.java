package eu.binarystars.autovalidate.userselfservice.account.domain;

import eu.binarystars.autovalidate.userselfservice.account.domain.SelfServiceException.Reason;
import eu.binarystars.autovalidate.userselfservice.account.repository.AccountRepositoryInMemImpl;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceTest {

    private static final Instant START = Instant.parse("2026-01-01T12:00:00Z");

    private Instant now;
    private List<Account> mails;
    private AccountRepository repository;
    private AccountService service;

    @BeforeEach
    public void init() {
        now = START;
        mails = new ArrayList<>();
        repository = new AccountRepositoryInMemImpl();
        final Clock clock = new Clock() {
            public ZoneId getZone() { return ZoneOffset.UTC; }
            public Clock withZone(final ZoneId zone) { return this; }
            public Instant instant() { return now; }
        };
        service = new AccountService(repository, mails::add, NoOpPasswordEncoder.getInstance(), clock);
    }

    private static RegistrationData registrationData(final String username, final String email) {
        return new RegistrationData(username, email, "gabba-gabba-hey", "gabba-gabba-hey", true);
    }

    private void assertFails(final Reason expected, final Runnable action) {
        assertEquals(expected, assertThrows(SelfServiceException.class, action::run).getReason());
    }

    @Test
    public void signUp_OK() {
        final Account account = service.signUp(registrationData("joey", "joey@ramones.com"));

        assertEquals("joey", account.getUsername());
        assertEquals(AccountStatus.NEW, account.getStatus());
        assertEquals(START, account.getTermsAcceptedAt());
        assertNotNull(account.getEmailVerificationToken());
        assertEquals(START.plus(AccountService.TOKEN_VALIDITY), account.getEmailVerificationTokenExpiresAt());
        assertSame(account, repository.findByUsername("joey").orElseThrow());
        assertEquals(List.of(account), mails);
    }

    @Test
    public void signUp_invalidData() {
        // These are not checked by the service - the Account refuses to exist.
        assertThrows(ConstraintViolationException.class,
                () -> service.signUp(registrationData("joey", "joey_at_ramones.com")));
        assertThrows(ConstraintViolationException.class,
                () -> service.signUp(registrationData("j", "joey@ramones.com")));
        assertThrows(ConstraintViolationException.class,
                () -> service.signUp(registrationData(null, null)));
        assertTrue(mails.isEmpty());
    }

    @Test
    public void signUp_rejected() {
        service.signUp(registrationData("joey", "joey@ramones.com"));

        assertFails(Reason.USERNAME_TAKEN,
                () -> service.signUp(registrationData("joey", "other@ramones.com")));
        assertFails(Reason.PASSWORD_TOO_SHORT,
                () -> service.signUp(new RegistrationData("dee-dee", "deedee@ramones.com", "1234", "1234", true)));
        assertFails(Reason.PASSWORDS_DO_NOT_MATCH,
                () -> service.signUp(new RegistrationData("dee-dee", "deedee@ramones.com", "gabba-gabba-hey", "gabba-gabba-hoo", true)));
        assertFails(Reason.TERMS_NOT_ACCEPTED,
                () -> service.signUp(new RegistrationData("dee-dee", "deedee@ramones.com", "gabba-gabba-hey", "gabba-gabba-hey", false)));
        assertEquals(1, mails.size());
    }

    @Test
    public void verifyEmail_OK() {
        final Account account = service.signUp(registrationData("joey", "joey@ramones.com"));
        final String token = account.getEmailVerificationToken();
        assertFails(Reason.ACCOUNT_NOT_VERIFIED, () -> service.login("joey", "gabba-gabba-hey"));

        now = START.plus(Duration.ofHours(1));
        final Account verified = service.verifyEmail(token);

        assertNotSame(account, verified);
        assertEquals(account.getId(), verified.getId());
        assertEquals(AccountStatus.VERIFIED, verified.getStatus());
        assertNull(verified.getEmailVerificationToken());
        assertSame(verified, repository.findByUsername("joey").orElseThrow());
        assertEquals(AccountStatus.NEW, account.getStatus(), "The original must not have changed.");

        assertSame(verified, service.login("joey", "gabba-gabba-hey"));
        assertFails(Reason.BAD_CREDENTIALS, () -> service.login("joey", "wrong"));
        assertFails(Reason.BAD_CREDENTIALS, () -> service.login("nobody", "gabba-gabba-hey"));
        // a token can be used only once
        assertFails(Reason.TOKEN_UNKNOWN, () -> service.verifyEmail(token));
    }

    @Test
    public void verifyEmail_rejected() {
        final Account account = service.signUp(registrationData("joey", "joey@ramones.com"));

        assertFails(Reason.TOKEN_UNKNOWN, () -> service.verifyEmail("no-such-token"));
        assertFails(Reason.TOKEN_UNKNOWN, () -> service.verifyEmail(null));

        now = START.plus(AccountService.TOKEN_VALIDITY).plusSeconds(1);
        assertFails(Reason.TOKEN_EXPIRED, () -> service.verifyEmail(account.getEmailVerificationToken()));
        assertEquals(AccountStatus.NEW, repository.findByUsername("joey").orElseThrow().getStatus());
    }
}
