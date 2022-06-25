package cloud;

public class DeleteFile implements CloudMessage{
    private String path;
    private boolean status;

    public DeleteFile(String path) {
        this.path = path;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public boolean getStatus() {
        return status;
    }
}
