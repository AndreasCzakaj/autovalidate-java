package eu.binarystars.autovalidate.userselfservice.account.rest;


import eu.binarystars.autovalidate.userselfservice.account.domain.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/accounts")
class AccountController {

    private final AccountService accountService;

    AccountController(final AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResource signUp(@RequestBody final RegistrationData registrationData) {
        return AccountResource.of(accountService.signUp(registrationData));
    }

    @PostMapping("/verify-email")
    public AccountResource verifyEmail(@RequestParam final String token) {
        return AccountResource.of(accountService.verifyEmail(token));
    }

    @PostMapping("/login")
    public AccountResource login(@RequestBody final LoginData loginData) {
        return AccountResource.of(accountService.login(loginData.username(), loginData.password()));
    }


    @ExceptionHandler
    ResponseEntity<Map<String, Object>> handle(final ConstraintViolationException e) {
        final List<String> violations = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .toList();
        return ResponseEntity.badRequest().body(Map.of("error", "INVALID_DATA", "violations", violations));
    }

    @ExceptionHandler
    ResponseEntity<Map<String, Object>> handle(final SelfServiceException e) {
        final HttpStatus status = switch (e.getReason()) {
            case USERNAME_TAKEN -> HttpStatus.CONFLICT;
            case PASSWORD_TOO_SHORT, PASSWORDS_DO_NOT_MATCH, TERMS_NOT_ACCEPTED -> HttpStatus.BAD_REQUEST;
            case TOKEN_UNKNOWN -> HttpStatus.NOT_FOUND;
            case TOKEN_EXPIRED -> HttpStatus.GONE;
            case BAD_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case ACCOUNT_NOT_VERIFIED -> HttpStatus.FORBIDDEN;
        };
        return ResponseEntity.status(status).body(Map.of("error", e.getReason().name()));
    }
}
