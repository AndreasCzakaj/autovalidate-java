package com.acnebs.autovalidate.restexampl.room.rest;


import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import lombok.*;
import lombok.experimental.*;

import java.util.List;

@Getter(value = AccessLevel.PUBLIC)
@Setter(value = AccessLevel.PUBLIC)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class RoomResource {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    String name;
    String number;
    Integer feelGoodCapacity;
    Integer maximumCapacity;

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static RoomResource fromJson(final String json) {
        try {
            return objectMapper.readValue(json, RoomResource.class);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<RoomResource> listFromJson(final String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<RoomResource>>() {});
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

}
