package files;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgresConnector {

    private static final String URL = "jdbc:postgresql://localhost:5432/MetroApp";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Подключение к базе данных прошло успешно!");
        } catch (SQLException e) {
            System.err.println("Ошибка подключения к базе данных:");
            e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        connect();
    }
}
