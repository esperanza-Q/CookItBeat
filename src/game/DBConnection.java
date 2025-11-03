package game;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/"+System.getenv("DB_NAME"); // DB 이름
    private static final String USER = System.getenv("DB_USERNAME"); // MySQL 사용자명
    private static final String PASSWORD = System.getenv("DB_PASSWORD"); // MySQL 비밀번호

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}