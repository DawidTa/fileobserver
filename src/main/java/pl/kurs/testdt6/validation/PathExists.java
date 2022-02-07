package pl.kurs.testdt6.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PathExistsValidator.class)
public @interface PathExists {
    String message() default "Path not exists or is a  directory.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
