package eu.binarystars.autovalidate.userselfservice.account.domain;


public interface VerificationMailer {

    void sendVerificationMail(Account account);
}
