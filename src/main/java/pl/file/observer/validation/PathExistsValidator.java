package pl.file.observer.validation;

import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;

@RequiredArgsConstructor
public class PathExistsValidator implements ConstraintValidator<PathExists, String> {

    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintValidatorContext) {
        File file = new File(path);
        return file.exists() && !file.isDirectory();
    }
}
