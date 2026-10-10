package eu.binarystars.autovalidate.userselfservice.account.rest;


import eu.binarystars.autovalidate.userselfservice.account.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    private ResultActions postJson(final String path, final Object body) throws Exception {
        return mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private static RegistrationData registrationData(final String username, final String email) {
        return new RegistrationData(username, email, "gabba-gabba-hey", "gabba-gabba-hey", true);
    }

    @Test
    public void signUp_verify_login_OK() throws Exception {
        final LoginData loginData = new LoginData("joey", "gabba-gabba-hey");

        postJson("/accounts/signup", registrationData("joey", "joey@ramones.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.username").value("joey"))
                .andExpect(jsonPath("$.email").value("joey@ramones.com"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.emailVerificationToken").doesNotExist());

        postJson("/accounts/login", loginData)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCOUNT_NOT_VERIFIED"));

        // the token is sent by mail, so we have to peek into the repository
        final String token = accountRepository.findByUsername("joey").orElseThrow().getEmailVerificationToken();
        mockMvc.perform(post("/accounts/verify-email").param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));

        postJson("/accounts/login", loginData)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joey"));
        postJson("/accounts/login", new LoginData("joey", "wrong"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void signUp_NOK() throws Exception {
        // rejected by the auto-validated Account
        postJson("/accounts/signup", registrationData("johnny", "johnny_at_ramones.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_DATA"))
                .andExpect(jsonPath("$.violations.length()").value(1));

        // rejected by the service
        postJson("/accounts/signup", new RegistrationData("johnny", "johnny@ramones.com", "gabba-gabba-hey", "typo", true))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PASSWORDS_DO_NOT_MATCH"));

        postJson("/accounts/signup", registrationData("johnny", "johnny@ramones.com"))
                .andExpect(status().isCreated());
        postJson("/accounts/signup", registrationData("johnny", "other@ramones.com"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("USERNAME_TAKEN"));
    }

    @Test
    public void verifyEmail_NOK() throws Exception {
        mockMvc.perform(post("/accounts/verify-email").param("token", "no-such-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("TOKEN_UNKNOWN"));
    }
}
