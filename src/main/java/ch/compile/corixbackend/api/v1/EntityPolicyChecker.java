package ch.compile.corixbackend.api.v1;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import ch.compile.corixbackend.api.v1.EntityPolicy.EditableIf;
import ch.compile.corixbackend.api.v1.EntityPolicy.VisibleIf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PolicyChecker is a generic class that checks if a field has been changed and
 * if it has violated the policy.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EntityPolicyChecker {
    private final FieldComparer fieldComparer;
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Checks if a field has been changed and if it has violated the policy.
     * Every changed field is checked for violations.
     * All violations are returned.
     * 
     * @param <T>      Type of the input object
     * @param oldValue
     * @param newValue
     * @throws PolicyViolationException when a policy has been violated
     */
    public <T> void checkPolicyViolation(T oldValue, T newValue) throws PolicyViolationException {
        Set<Field> changedFields = fieldComparer.changedFields(oldValue, newValue);
        Set<Violation> violations = new HashSet<>();

        for (Field f : changedFields) {
            for (Class<? extends Annotation> annotationClass : EntityPolicy.entityPolicies) {
                if (f.isAnnotationPresent(annotationClass)) {
                    switch (f.getAnnotation(annotationClass)) {
                        case EditableIf annotation -> {
                            String spelExpression = annotation.expression();

                            Boolean noViolation = parser.parseExpression(spelExpression)
                                    .getValue(oldValue, Boolean.class);

                            if (noViolation == null) {
                                throw new IllegalStateException(
                                        "CorixPolicyViolation: " + spelExpression + " could not be evaluated.");
                            }

                            if (noViolation) {
                                log.debug("No Violation field={} expression={} for oldObj={} newObj={}", f,
                                        spelExpression, oldValue, newValue);
                            } else {
                                violations.add(new Violation(f, annotation, spelExpression));
                                log.debug("Violation field={} expression={} for oldObj={} newObj={}", f, spelExpression,
                                        oldValue, newValue);
                            }
                        }

                        case VisibleIf annotation -> {
                            String spelExpression = annotation.expression();

                            Boolean noViolation = parser.parseExpression(spelExpression)
                                    .getValue(oldValue, Boolean.class);

                            if (noViolation == null) {
                                throw new IllegalStateException(
                                        "CorixPolicyViolation: " + spelExpression + " could not be evaluated.");
                            }

                            if (noViolation) {
                                log.debug("No Violation field={} expression={} for oldObj={} newObj={}", f,
                                        spelExpression, oldValue, newValue);
                            } else {
                                violations.add(new Violation(f, annotation, spelExpression));
                                log.debug("Violation field={} expression={} for oldObj={} newObj={}", f, spelExpression,
                                        oldValue, newValue);
                            }
                        }

                        default -> throw new IllegalStateException("Unknown annotation " + annotationClass);
                    }
                }
            }
        }

        if (violations.size() > 0) {
            throw new PolicyViolationException(violations);
        }
    }

    public record Violation(Field field, Annotation policy, String expression) {
    }

    /**
     * CorixEditablePolicyViolation is thrown when a policy has been violated.
     */
    @RequiredArgsConstructor
    @Getter
    public class PolicyViolationException extends Exception {
        private final Set<Violation> violations;

        @Override
        public String getMessage() {
            return "CorixEditablePolicyViolation: "
                    + violations.stream().map(Violation::toString).collect(Collectors.joining(", "));
        }
    }

}
