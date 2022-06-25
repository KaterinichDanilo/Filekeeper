package netty.handler;

import java.sql.*;
import java.util.logging.Level;

public class UsersData {
    private static Connection connection;
    private static Statement statement;
    private static final String NAME = "root";
    private static final String PASSWORD = "root";
    private static final String URL = "jdbc:mysql://localhost:3306/usersdata";
    private static PreparedStatement preparedInsertStatement;
    private static PreparedStatement preparedUpdateStatement;
    private static PreparedStatement preparedStatementGetPassword;

    public UsersData() {
        try {
            connect();
            prepareStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void connect() throws Exception{
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(URL, NAME, PASSWORD);
        statement = connection.createStatement();
        System.out.println("Database connected!");
    }

    private static void prepareStatement() throws SQLException {
        preparedInsertStatement = connection.prepareStatement("INSERT INTO users (login, password) VALUES (?, ?);");
        preparedStatementGetPassword = connection.prepareStatement("SELECT password FROM users WHERE login = ?;");
    }

    public String getPassword(String login) {
        try {
            preparedStatementGetPassword.setString(1, login);
            ResultSet resultSet = preparedStatementGetPassword.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean registration(String login, String password) {
        try {
            preparedInsertStatement.setString(1, login);
            preparedInsertStatement.setString(2, password);
            preparedInsertStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

}
