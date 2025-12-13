package university.scenario; // ⚠️ 패키지 위치가 scenario라면 이걸 쓰세요. (util이라면 util로 변경)

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionManager {

    // DB 연결 정보 (본인 환경에 맞게 수정 필요)
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "c##park2"; // 본인 아이디
    private static final String PASSWORD = "park"; // 본인 비번

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}