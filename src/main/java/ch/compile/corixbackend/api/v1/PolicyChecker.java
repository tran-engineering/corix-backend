package ch.compile.corixbackend.api.v1;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyChecker {
    private final FieldComparer fieldComparer;
    private final ExpressionParser parser = new SpelExpressionParser();

    public <T> Set<Violation> checkPolicyViolation(T o1, T o2) {
        Set<Field> changedFields = fieldComparer.changedFields(o1, o2);
        Set<Violation> violations = new HashSet<>();
        EvaluationContext context = new StandardEvaluationContext();
        for (Field field: o1.getClass().getDeclaredFields()) {
            try {
                context.setVariable(field.getName(), field.get(o1));
            } catch(IllegalAccessException e) {
                log.warn("Could not access {}", field);
            }
            
        }

        for (Field f : changedFields) {
            if (f.isAnnotationPresent(CorixEditable.class)) {
                String spelExpression = f.getAnnotation(CorixEditable.class).value();
                boolean b = parser.parseExpression(spelExpression).getValue(context, Boolean.class);
                if (b) {
                    log.info("TRUE");
                } else {
                    violations.add(new Violation(f, spelExpression));
                    log.info("FALSE");
                }
            }
        }
        return violations;
    }

    public record Violation(Field field, String expression) {
    }
}
