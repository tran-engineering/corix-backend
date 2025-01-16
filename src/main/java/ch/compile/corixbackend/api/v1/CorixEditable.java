package ch.compile.corixbackend.api.v1;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CorixEditable is an Annotation that describes when a field can be edited.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface CorixEditable {
    /**
     * @return SpEl expression to run on the old document
     */
    String value();
}
