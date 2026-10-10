package com.acnebs.autovalidate.restexampl.room.domain;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;

public class RoomTest {

    @Test
    public void test_validation() throws Exception {
        try {
            Room.builder().build();
            fail("It should fail");
        }
        catch (ConstraintViolationException e) {
            assertEquals(3, e.getConstraintViolations().size());
        }
    }



}
