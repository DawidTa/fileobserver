package pl.kurs.testdt6.file;

import lombok.Data;

import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

@Data
public class FileModel {
    private Map<String, WatchService> watchServices;

    public FileModel() {
        this.watchServices = new HashMap<>();
    }
}
