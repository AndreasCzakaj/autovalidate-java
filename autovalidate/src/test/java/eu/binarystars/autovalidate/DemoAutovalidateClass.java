package eu.binarystars.autovalidate;


import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.*;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Builder(toBuilder=true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@AutoValidate
class DemoAutovalidateClass {

    @Size(min = 1, max = 256)
    String firstName;

    @NotNull
    @Size(min = 1, max = 256)
    String lastName;

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$")
    String email;
}
