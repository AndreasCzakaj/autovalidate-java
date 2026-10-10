package eu.binarystars.autovalidate.userselfservice.account.domain;


public class SelfServiceException extends RuntimeException {

    public enum Reason {
        USERNAME_TAKEN,
        PASSWORD_TOO_SHORT,
        PASSWORDS_DO_NOT_MATCH,
        TERMS_NOT_ACCEPTED,
        TOKEN_UNKNOWN,
        TOKEN_EXPIRED,
        BAD_CREDENTIALS,
        ACCOUNT_NOT_VERIFIED
    }

    private final Reason reason;

    public SelfServiceException(final Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
