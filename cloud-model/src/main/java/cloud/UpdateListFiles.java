package cloud;

public class UpdateListFiles implements CloudMessage{
    private String login;

    public UpdateListFiles(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }
}
