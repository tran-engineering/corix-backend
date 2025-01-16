package ch.compile.corixbackend.api.v1;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PolicyChecker is a generic class that checks if a field has been changed and if it has violated the policy.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyChecker {
    private final FieldComparer fieldComparer;
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Checks if a field has been changed and if it has violated the policy.
     * Every changed field is checked for violations.
     * All violations are returned.
     * 
     * @param <T> Type of the input object
     * @param oldValue
     * @param newValue
     * @throws CorixEditablePolicyViolation when a policy has been violated
     */
    public <T> void checkPolicyViolation(T oldValue, T newValue) throws CorixEditablePolicyViolation {
        Set<Field> changedFields = fieldComparer.changedFields(oldValue, newValue);
        Set<Violation> violations = new HashSet<>();

        for (Field f : changedFields) {
            if (f.isAnnotationPresent(CorixEditable.class)) {
                String spelExpression = f.getAnnotation(CorixEditable.class).value();
                
                Boolean noViolation = parser.parseExpression(spelExpression)
                    .getValue(oldValue, Boolean.class);

                if (noViolation == null) {
                    throw new IllegalStateException("CorixPolicyViolation: " + spelExpression + " could not be evaluated.");
                }
                
                if (noViolation) {
                    log.debug("No Violation field={} expression={} for oldObj={} newObj={}", f, spelExpression, oldValue, newValue);
                } else {
                    violations.add(new Violation(f, spelExpression));
                    log.debug("Violation field={} expression={} for oldObj={} newObj={}", f, spelExpression, oldValue, newValue);
                }
            }
        }

        if (violations.size() > 0) {
            throw new CorixEditablePolicyViolation(violations);
        }
    }

    public record Violation(Field field, String expression) {
    }
}
