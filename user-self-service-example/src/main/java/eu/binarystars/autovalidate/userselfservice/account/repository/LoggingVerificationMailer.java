package eu.binarystars.autovalidate.userselfservice.account.repository;


import eu.binarystars.autovalidate.userselfservice.account.domain.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;

/**
 * Stands in for a real mail sender: it just logs the verification token.
 */
@Component
public class LoggingVerificationMailer implements VerificationMailer {
    private static final Logger log = LoggerFactory.getLogger(LoggingVerificationMailer.class);

    @Override
    public void sendVerificationMail(final Account account) {
        log.info("Verification token for {} <{}>: {}",
                account.getUsername(), account.getEmail(), account.getEmailVerificationToken());
    }
}
