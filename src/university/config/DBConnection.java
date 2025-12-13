package university.config;

import java.sql.*;

/**
 * 데이터베이스 연결 관리 클래스
 * Singleton 패턴 적용
 */
public class DBConnection {

    // Oracle JDBC 설정
    private static final String DRIVER = "oracle.jdbc.OracleDriver";
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/xe";
    private static final String USER = "c##park2";
    private static final String PASSWORD = "1234";

    // Singleton 인스턴스
    private static DBConnection instance;
    private Connection connection;

    // private 생성자 (외부에서 인스턴스 생성 방지)
    private DBConnection() {
        try {
            Class.forName(DRIVER);
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ 데이터베이스 연결 성공");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ JDBC 드라이버를 찾을 수 없습니다: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (SQLException e) {
            System.err.println("✗ 데이터베이스 연결 실패: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * DBConnection 인스턴스 반환 (Singleton)
     */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Connection 객체 반환
     */
    public Connection getConnection() {
        try {
            // 연결이 끊어졌으면 재연결
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✓ 데이터베이스 재연결 성공");
            }
        } catch (SQLException e) {
            System.err.println("✗ 연결 상태 확인 실패: " + e.getMessage());
        }
        return connection;
    }

    /**
     * 리소스 정리 메서드
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("✗ 리소스 종료 실패: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 트랜잭션 시작
     */
    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            System.err.println("✗ 트랜잭션 시작 실패: " + e.getMessage());
        }
    }

    /**
     * 트랜잭션 커밋
     */
    public void commit() {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("✗ 커밋 실패: " + e.getMessage());
        }
    }

    /**
     * 트랜잭션 롤백
     */
    public void rollback() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("✗ 롤백 실패: " + e.getMessage());
        }
    }

    /**
     * 연결 종료
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ 데이터베이스 연결 종료");
            }
        } catch (SQLException e) {
            System.err.println("✗ 연결 종료 실패: " + e.getMessage());
        }
    }
}