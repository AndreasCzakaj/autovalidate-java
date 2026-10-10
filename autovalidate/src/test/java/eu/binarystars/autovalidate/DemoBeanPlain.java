package eu.binarystars.autovalidate;


import jakarta.validation.constraints.*;

class DemoBeanPlain {

    @Size(min = 1, max = 256)
    private String firstName;

    String getFirstName() {
        return firstName;
    }

    DemoBeanPlain setFirstName(final String firstName) {
        this.firstName = firstName;
        return this;
    }


    @NotNull
    @Size(min = 1, max = 256)
    private String lastName;


    String getLastName() {
        return lastName;
    }

    DemoBeanPlain setLastName(final String lastName) {
        this.lastName = lastName;
        return this;
    }


    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
    private String email;

    String getEmail() {
        return email;
    }

    DemoBeanPlain setEmail(final String email) {
        this.email = email;
        return this;
    }
}
