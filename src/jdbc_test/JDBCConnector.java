package jdbc_test;

import java.sql.*;

public class JDBCConnector {
    private static final String DRIVER_PATH = "oracle.jdbc.OracleDriver";
    private static final String URL = "jdbc:oracle:thin:@localhost:1521/xe";
    private static final String USER_NAME = "c##park";
    private static final String PASSWORD = "1234";

    private static Connection con;

    public static Connection getConnection() {

        try {
//        1. JDBC Driver Memory Loading
            Class.forName(DRIVER_PATH);
            System.out.println("JDBC Driver Loaded");
//        2. Connection
            con = DriverManager.getConnection(URL, USER_NAME, PASSWORD);
            System.out.println("Connection Done Successfully");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver Class not found");
        } catch (SQLException e) {
            System.out.println("Connection Error");
        }
        return con;
    }

    public static void resultSetTest() {

        try {
//        3.SQL문을 실행할 수 있는 Statement객체 생성(미리준비된 sql문을 실행할 수 있는 문장객체)
            String sql = "select id, name, publish_name, author from book b, publish p where b.publish_id=p.publish_id";
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.print(rs.getString("name") + " || ");
                System.out.print(rs.getString("publish_name"));
                System.out.println();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Connection con = getConnection();
        resultSetTest();
    }
}
