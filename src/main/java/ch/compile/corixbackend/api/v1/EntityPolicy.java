package ch.compile.corixbackend.api.v1;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

/**
 * Just a static class that contains all the policy annotations
 */
public class EntityPolicy {

    private EntityPolicy() {
    }

    public final static Set<Class<? extends Annotation>> REGISTERED_POLICIES = Set.of(
            EditableIf.class,
            VisibleIf.class);

    /**
     * EditableIf expression should return true when the field can be edited
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.FIELD })
    public @interface EditableIf {
        String expression();
    }

    /**
     * VisibleIf expression should return true when the field should be visible
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.FIELD })
    public @interface VisibleIf {
        String expression();
    }
}
