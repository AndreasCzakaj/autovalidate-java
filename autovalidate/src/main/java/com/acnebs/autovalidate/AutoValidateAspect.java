package com.acnebs.autovalidate;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

import jakarta.validation.*;
import java.util.Set;


@Aspect
public class AutoValidateAspect {
    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @AfterReturning("execution(*.new(..)) && within(@AutoValidate *) && !within(*.*Builder)")
    public <T> void afterClassAnnotation(JoinPoint joinPoint) throws Throwable {
        validate(joinPoint);
    }

    @AfterReturning("execution(*.new(..)) && @annotation(AutoValidate)")
    public <T> void afterConstructorAnnotation(JoinPoint joinPoint) throws Throwable {
        validate(joinPoint);
    }

    private <T> void validate(JoinPoint joinPoint) throws Throwable {
        final T object = (T) joinPoint.getThis();
        final Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (violations.size() > 0) {
            throw new ConstraintViolationException(violations);
        }
    }
}
