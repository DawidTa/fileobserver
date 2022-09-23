package pl.file.observer.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHelper {

    private final ExceptionService exceptionService;

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ExceptionModel model = exceptionService.setExceptionResponse(ex, HttpStatus.BAD_REQUEST);
        model.setMessage(Objects.requireNonNull(ex.getFieldError()).getDefaultMessage());
        return new ResponseEntity<>(model, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {JobNotFoundException.class})
    public ResponseEntity handleJobNotFoundException(JobNotFoundException ex) {
        ExceptionModel model = exceptionService.setExceptionResponse(ex, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(model, HttpStatus.NOT_FOUND);
    }
}