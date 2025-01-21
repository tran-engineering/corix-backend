package ch.compile.corixbackend.api.v1;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

public interface EntityPolicy extends Annotation {

    public final static Set<Class<? extends Annotation>> entityPolicies = Set.of(
        EditableIf.class,
        VisibleIf.class
    );
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.FIELD})
    public @interface EditableIf {
        String expression();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.FIELD})
    public @interface VisibleIf {
        String expression();
    }
}
