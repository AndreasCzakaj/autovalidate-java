package eu.binarystars.autovalidate.userselfservice.account.rest;


import eu.binarystars.autovalidate.userselfservice.account.domain.*;

import java.util.UUID;

/**
 * What the API reveals about an account - neither the password hash nor the verification token.
 */
record AccountResource(UUID id, String username, String email, AccountStatus status) {

    static AccountResource of(final Account account) {
        return new AccountResource(account.getId(), account.getUsername(), account.getEmail(), account.getStatus());
    }
}
