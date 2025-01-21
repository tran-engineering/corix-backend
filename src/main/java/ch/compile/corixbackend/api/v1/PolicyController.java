package ch.compile.corixbackend.api.v1;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.compile.corixbackend.api.v1.EntityPolicy.EditableIf;
import ch.compile.corixbackend.api.v1.EntityPolicy.VisibleIf;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/policy")
public class PolicyController {

    private final EntityManager entityManager;

    @Bean
    public GroupedOpenApi policyApi() {
        return GroupedOpenApi.builder()
                .group("policy-api")
                .pathsToMatch("/api/v1/policy/**")
                .build();
    }

    @GetMapping(produces = "application/json")
    @Operation(summary = "Returns all policies")
    @ApiResponse(
        responseCode = "200",
        content = @Content(
        examples = @ExampleObject("""
            {
                "SomeEntity" : {
                    "EditableIf: {
                        "someField" : "otherField == 'someValue'"
                    }
                }
            }
        """)
        ))
    public Map<String, Map<String, Map<String, String>>> getPolicies() {
        List<PolicyDefinition> allPolicies = entityManager.getMetamodel().getEntities().stream()
                .map(entity -> getPolicyForClass(entity.getJavaType()))
                .flatMap(List::stream)
                .toList();

        return allPolicies.stream()
                .collect(
                    groupingBy(d -> d.clazz().getSimpleName(), 
                    groupingBy(d -> d.annotationClass.getSimpleName(),
                    toMap(d -> d.field.getName(), d-> d.expression()))));
    }

    private List<PolicyDefinition> getPolicyForClass(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields()).flatMap(field -> {
            return EntityPolicy.entityPolicies.stream()
                    .filter(annotationClass -> field.isAnnotationPresent(annotationClass))
                    .map(annotationClass -> switch (field.getAnnotation(annotationClass)) {
                        case EditableIf ann -> new PolicyDefinition(clazz, field, annotationClass, ann.expression());
                        case VisibleIf ann -> new PolicyDefinition(clazz, field, annotationClass, ann.expression());
                        default -> throw new IllegalAccessError();
                    });
        }).toList();
    }

    private record PolicyDefinition(
            Class<?> clazz,
            Field field,
            Class<? extends Annotation> annotationClass,
            String expression
            ) {

    }
}
