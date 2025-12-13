package university.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MiniConnectionPool {
    // DB 정보 (본인 환경에 맞게 수정)
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/xe";
    private static final String USER = "c##park2";
    private static final String PASS = "1234";

    // 커넥션 10개 미리 만들어두는 보관함
    private static final BlockingQueue<Connection> connectionQueue = new ArrayBlockingQueue<>(10);

    static {
        try {
            System.out.println("🔥 [Pool] 커넥션 10개 생성 중...");
            for (int i = 0; i < 10; i++) {
                Connection conn = DriverManager.getConnection(URL, USER, PASS);
                conn.setAutoCommit(false);
                connectionQueue.offer(conn);
            }
            System.out.println("✅ [Pool] 준비 완료!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static Connection getConnection() throws Exception {
        // 3초 기다려보고 없으면 에러
        Connection conn = connectionQueue.poll(3, TimeUnit.SECONDS);
        if (conn == null) throw new SQLException("연결 풀 꽉 참 (대기 시간 초과)");
        return conn;
    }

    public static void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback(); // 쓴 거 깨끗이 씻어서
                connectionQueue.offer(conn); // 다시 보관함에 넣기
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}