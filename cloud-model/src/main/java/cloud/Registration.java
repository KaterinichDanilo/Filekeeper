package cloud;

public class Registration implements CloudMessage{
    private String login;
    private String password;
    private boolean regStatus;

    public Registration(boolean regStatus) {
        this.regStatus = regStatus;
    }

    public void setRegStatus(boolean regStatus) {
        this.regStatus = regStatus;
    }

    public boolean getRegStatus() {
        return regStatus;
    }

    public Registration(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
