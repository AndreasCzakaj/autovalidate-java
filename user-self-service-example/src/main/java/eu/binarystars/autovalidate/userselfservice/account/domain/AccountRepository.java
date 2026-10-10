package eu.binarystars.autovalidate.userselfservice.account.domain;


import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmailVerificationToken(String token);

    void save(Account account);
}
