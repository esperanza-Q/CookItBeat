package game;

import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/"+System.getenv("DB_NAME"); // DB 이름
    private static final String USER = System.getenv("DB_USERNAME"); // MySQL 사용자명
    private static final String PASSWORD = System.getenv("DB_PASSWORD"); // MySQL 비밀번호
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    public static Connection getConnection() {
        // 환경 변수 설정 여부 미리 검사 (선택 사항, 견고성 향상)
        if (URL == null || USER == null || PASSWORD == null) {
            throw new RuntimeException("DB 연결 정보(환경 변수)가 설정되지 않았습니다.");
        }

        try {
            // 1. 드라이버 로드
            Class.forName(DRIVER);

            // 2. 연결 시도 및 Connection 객체 반환
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            // 드라이버 JAR 파일이 클래스패스에 없는 경우
            throw new RuntimeException("MySQL JDBC 드라이버를 찾을 수 없습니다.", e);

        } catch (SQLException e) {
            // 연결 정보 오류, 접근 거부 등 SQL 관련 문제 발생 시
            throw new RuntimeException("데이터베이스 연결에 실패했습니다: " + e.getMessage(), e);
        }
    }

    // 로그인 검증
    public static boolean validateLogin(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // 결과가 있으면 true

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registerUser(String username) {

        // 1. 중복 아이디 체크
        String checkSql = "SELECT username FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // 이미 존재하는 아이디
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // 2. 회원 등록
        String insertSql = "INSERT INTO users (username) VALUES (?)";

        try (Connection conn = getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            insertStmt.setString(1, username);

            int rows = insertStmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 점수 저장
    public static void saveScore(String username, int score) {
        String sql = "INSERT INTO scores (username, score, date) VALUES (?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}