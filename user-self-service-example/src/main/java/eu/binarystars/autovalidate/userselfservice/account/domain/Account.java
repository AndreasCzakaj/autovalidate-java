package eu.binarystars.autovalidate.userselfservice.account.domain;

import eu.binarystars.autovalidate.AutoValidate;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

/**
 * The central entity. It is immutable and auto-validated, i.e. if an instance exists it is valid.
 * To "change" an account create a copy with {@link #toBuilder()} - the copy is validated as well.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Builder(toBuilder=true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@AutoValidate
public class Account {

    @NotNull
    UUID id;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_.-]{3,32}$")
    String username;

    @NotBlank
    String passwordHash;

    @NotNull
    @Size(max = 256)
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
    String email;

    @NotNull
    @PastOrPresent
    Instant termsAcceptedAt;

    @NotNull
    AccountStatus status;

    String emailVerificationToken;

    Instant emailVerificationTokenExpiresAt;

    // A rule that spans several fields: an unverified account needs a token to get verified with.
    @AssertTrue(message = "a new account must have an email verification token and its expiration date")
    boolean isEmailVerificationTokenPresentWhileNew() {
        return status != AccountStatus.NEW
                || (emailVerificationToken != null && emailVerificationTokenExpiresAt != null);
    }
}
