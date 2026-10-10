package eu.binarystars.autovalidate.userselfservice.account.domain;


import eu.binarystars.autovalidate.userselfservice.account.domain.SelfServiceException.Reason;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.*;
import java.util.*;

@Service
public class AccountService {
    static final int MIN_PASSWORD_LENGTH = 8;
    static final Duration TOKEN_VALIDITY = Duration.ofHours(24);

    private final AccountRepository repository;
    private final VerificationMailer mailer;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    AccountService(
            final AccountRepository repository,
            final VerificationMailer mailer,
            final PasswordEncoder passwordEncoder,
            final Clock clock
    ) {
        this.repository = repository;
        this.mailer = mailer;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    public synchronized Account signUp(final RegistrationData data) {
        // The plain password never becomes part of the account, so it must be checked here.
        if (data.password() == null || data.password().length() < MIN_PASSWORD_LENGTH) {
            throw new SelfServiceException(Reason.PASSWORD_TOO_SHORT);
        }
        if (!data.password().equals(data.passwordRepeated())) {
            throw new SelfServiceException(Reason.PASSWORDS_DO_NOT_MATCH);
        }
        if (!data.termsAccepted()) {
            throw new SelfServiceException(Reason.TERMS_NOT_ACCEPTED);
        }
        if (repository.findByUsername(data.username()).isPresent()) {
            throw new SelfServiceException(Reason.USERNAME_TAKEN);
        }

        final Instant now = clock.instant();
        // No explicit validation of username and email: if they are invalid then build() throws
        // a ConstraintViolationException and the account never comes into existence.
        final Account account = Account.builder()
                .id(UUID.randomUUID())
                .username(data.username())
                .passwordHash(passwordEncoder.encode(data.password()))
                .email(data.email())
                .termsAcceptedAt(now)
                .status(AccountStatus.NEW)
                .emailVerificationToken(newToken())
                .emailVerificationTokenExpiresAt(now.plus(TOKEN_VALIDITY))
                .build();

        repository.save(account);
        mailer.sendVerificationMail(account);
        return account;
    }

    public synchronized Account verifyEmail(final String token) {
        final Account account = repository.findByEmailVerificationToken(token)
                .filter(a -> a.getStatus() == AccountStatus.NEW)
                .orElseThrow(() -> new SelfServiceException(Reason.TOKEN_UNKNOWN));
        if (clock.instant().isAfter(account.getEmailVerificationTokenExpiresAt())) {
            throw new SelfServiceException(Reason.TOKEN_EXPIRED);
        }

        // The account is immutable, so we create a copy. The copy is auto-validated, too.
        final Account verified = account.toBuilder()
                .status(AccountStatus.VERIFIED)
                .emailVerificationToken(null)
                .emailVerificationTokenExpiresAt(null)
                .build();

        repository.save(verified);
        return verified;
    }

    public Account login(final String username, final String password) {
        final Account account = repository.findByUsername(username)
                .filter(a -> password != null && passwordEncoder.matches(password, a.getPasswordHash()))
                .orElseThrow(() -> new SelfServiceException(Reason.BAD_CREDENTIALS));
        if (account.getStatus() != AccountStatus.VERIFIED) {
            throw new SelfServiceException(Reason.ACCOUNT_NOT_VERIFIED);
        }
        return account;
    }

    private String newToken() {
        final byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
