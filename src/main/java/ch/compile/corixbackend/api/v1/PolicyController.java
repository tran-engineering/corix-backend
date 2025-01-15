package ch.compile.corixbackend.api.v1;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/policy")
public class PolicyController {

    @Bean
    public GroupedOpenApi policyApi() {
        return GroupedOpenApi.builder()
                .group("policy-api")
                .pathsToMatch("/api/v1/policy/**")
                .build();
    }

    @GetMapping()
    public Map<String, String> getTodo(
            @Parameter(example = "ch.compile.corixbackend.api.v1.Todo") String className) {

        try {
            Class<?> clazz1 = Class.forName(className);
            Map<String, String> policyMap = new HashMap<>();
            for (Field f : clazz1.getDeclaredFields()) {
                if (f.isAnnotationPresent(CorixEditable.class)) {
                    String value = f.getAnnotation(CorixEditable.class).value();
                    policyMap.put(f.getName(), value);
                } else {
                    policyMap.put(f.getName(), null);
                }
            }
            return policyMap;
        } catch (ClassNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found");
        }

    }

}
