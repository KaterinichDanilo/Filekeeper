package cloud;

import java.nio.file.Path;

public class CreateFolder implements CloudMessage{
    private String path;
    private String name;

    public CreateFolder(String path, String name) {
        this.path = path;
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
