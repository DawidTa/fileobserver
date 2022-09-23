package pl.file.observer.exception;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String jobUUID) {
        super("Job with provided id: " + jobUUID + " not exists or id is invalid");
    }
}
