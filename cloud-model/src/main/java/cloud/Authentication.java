package cloud;

public class Authentication implements CloudMessage{
    private boolean authStatus;
    private String login;
    private String password;

    public Authentication(boolean authStatus) {
        this.authStatus = authStatus;
    }

    public boolean getAuthStatus() {
        return authStatus;
    }

    public Authentication(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setAuthStatus(boolean authStatus) {
        this.authStatus = authStatus;
    }
}
