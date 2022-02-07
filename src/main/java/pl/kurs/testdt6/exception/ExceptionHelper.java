package pl.kurs.testdt6.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHelper {

    private final ExceptionService exceptionService;

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity handleIllegalArgumentException(IllegalArgumentException ex) {
        ExceptionModel model = exceptionService.setExceptionResponse(ex, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(model, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {JobNotFoundException.class})
    public ResponseEntity handleJobNotFoundException(JobNotFoundException ex) {
        ExceptionModel model = exceptionService.setExceptionResponse(ex, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(model, HttpStatus.NOT_FOUND);
    }
}