package eu.binarystars.autovalidate.userselfservice.account.domain;


/**
 * The raw sign-up input, deserialized from JSON. Nothing in here can be trusted yet.
 */
public record RegistrationData(
        String username,
        String email,
        String password,
        String passwordRepeated,
        boolean termsAccepted
) {
}
