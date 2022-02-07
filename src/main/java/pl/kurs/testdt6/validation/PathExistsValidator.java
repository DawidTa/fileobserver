package pl.kurs.testdt6.validation;

import lombok.RequiredArgsConstructor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;

@RequiredArgsConstructor
public class PathExistsValidator implements ConstraintValidator<PathExists, String> {

    @Override
    public boolean isValid(String path, ConstraintValidatorContext constraintValidatorContext) {
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("File not exist or is a directory");
        }
        return true;
    }
}
