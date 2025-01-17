package ch.compile.corixbackend.api.v1;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                    "someField" : "otherField == 'someValue'"
                }
            }
        """)
        ))
    public Map<String, Map<String, String>> getPolicies() {
        return entityManager.getMetamodel().getEntities().stream()
                .map(entity -> new EntityPolicy(entity.getJavaType(), getPolicyForClass(entity.getJavaType())))
                .collect(toMap(entityPolicy -> entityPolicy.clazz().getSimpleName(),
                        entityPolicy -> entityPolicy.policies()));
    }

    private Map<String, String> getPolicyForClass(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CorixEditable.class))
                .collect(toMap(field -> field.getName(), field -> field.getAnnotation(CorixEditable.class).value()));
    }

    private record EntityPolicy(Class<?> clazz, Map<String, String> policies) {
    }
}
