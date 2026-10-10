package eu.binarystars.autovalidate.userselfservice.account.repository;


import eu.binarystars.autovalidate.userselfservice.account.domain.*;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountRepositoryInMemImpl implements AccountRepository {
    private final Map<UUID, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public Optional<Account> findByUsername(final String username) {
        return accounts.values().stream()
                .filter(account -> account.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<Account> findByEmailVerificationToken(final String token) {
        return accounts.values().stream()
                .filter(account -> token != null && token.equals(account.getEmailVerificationToken()))
                .findFirst();
    }

    @Override
    public void save(final Account account) {
        accounts.put(account.getId(), account);
    }
}
