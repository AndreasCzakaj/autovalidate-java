package com.acnebs.autovalidate.restexampl.common.rest;


import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.hateoas.*;
import org.springframework.http.HttpEntity;

import java.util.Optional;
import java.util.function.Function;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

public abstract class ControllerBase {

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private final Function<Object, ? extends RepresentationModel<?>> halResourceFactory;
    private final Function<RepresentationModel<?>, String> idFactory;
    private final Class<?> resClass;

    protected ControllerBase(
            final Function<Object, RepresentationModel<?>> halResourceFactory,
            final Function<RepresentationModel<?>, String> idFactory,
            final Class<?> resClass
    ) {
        this.halResourceFactory = halResourceFactory;
        this.idFactory = idFactory;
        this.resClass = resClass;
    }

    protected <T extends RepresentationModel<?>> Optional<T> toHalResource(final Object object) {
        return Optional.ofNullable(object)
                .map(r -> objectMapper.convertValue(r, resClass))
                .map(halResourceFactory::apply)
                .map(hres -> {
                    hres.add(linkToWithSelfRel(idFactory.apply(hres)));
                    return (T) hres;
                });
    }

    private Link linkToWithSelfRel(final String number) {
        return linkTo(getLinkTarget(number)).withSelfRel();
    }

    private Link linkToWithRel(final String number, final String rel) {
        return linkTo(getLinkTarget(number)).withRel(rel);
    }

    protected abstract <T extends RepresentationModel<?>> HttpEntity<T> getLinkTarget(final String number);

}
