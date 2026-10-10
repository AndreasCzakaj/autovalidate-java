package com.acnebs.autovalidate.restexampl.room.domain;

import com.acnebs.autovalidate.AutoValidate;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.*;
import java.util.List;

@Getter
@Builder(toBuilder=true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@AutoValidate
@Schema
public class Room {

    @NotNull
    @Size(min = 1, max = 8)
    @Schema(description = "The room number. Used as a global ID.")
    String number;

    @Size(min = 1, max = 256)
    String name;

    @NotNull
    @Min(0)
    @Max(100)
    Integer feelGoodCapacity;

    @NotNull
    @Min(0)
    @Max(100)
    Integer maximumCapacity;


    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static Room fromJson(final String json) {
        try {
            return objectMapper.readValue(json, Room.class);
        } catch (JacksonException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                throw (ConstraintViolationException)e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    public static List<Room> listFromJson(final String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Room>>() {});
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
