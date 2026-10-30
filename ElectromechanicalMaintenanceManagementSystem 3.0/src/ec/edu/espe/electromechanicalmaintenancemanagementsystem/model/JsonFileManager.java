package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class JsonFileManager {
    private final Path filePath;

    public JsonFileManager(String fileName) {
        this(Path.of(fileName));
    }

    public JsonFileManager(Path filePath) {
        this.filePath = filePath;
    }

    public Path getFilePath() {
        return filePath;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> readObjects() {
        List<Map<String, Object>> objects = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return objects;
        }
        try {
            String text = Files.readString(filePath, StandardCharsets.UTF_8);
            if (text.trim().isEmpty()) {
                return objects;
            }
            Object root = Json.parse(text);
            if (root instanceof List) {
                for (Object item : (List<?>) root) {
                    if (item instanceof Map) {
                        objects.add((Map<String, Object>) item);
                    }
                }
            } else if (root instanceof Map) {
                objects.add((Map<String, Object>) root);
            }
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("Could not load " + filePath.getFileName() + ": " + exception.getMessage());
            keepCorruptCopy();
            objects.clear();
        }
        return objects;
    }

    public void writeObjects(List<Map<String, Object>> objects) {
        String json = Json.write(objects);
        try {
            Path absolute = filePath.toAbsolutePath();
            Path parent = absolute.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temporary = absolute.resolveSibling(absolute.getFileName() + ".tmp");
            Files.writeString(temporary, json, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            System.out.println("Could not save " + filePath.getFileName() + ": " + exception.getMessage());
        }
    }

   
    private void keepCorruptCopy() {
        try {
            Path copy = filePath.resolveSibling(filePath.getFileName() + ".corrupt");
            Files.copy(filePath, copy, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("A copy of the damaged file was kept as " + copy.getFileName());
        } catch (IOException ignored) {
          
        }
    }
}
