package university.scenario;

import university.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CascadeDeleteTest {

    public static void main(String[] args) {
        System.out.println("===  참조 무결성 및 CASCADE 삭제 테스트 ===");

        Connection conn = DBConnection.getInstance().getConnection();

        try {
            // 1. 교수 삭제 -> 개설 강좌 삭제 확인
            System.out.println("\n[Test 1] 교수(PROF001) 삭제 시도");
            System.out.println("   -> 예상: 해당 교수의 강의(99999)와 수강신청 내역이 같이 삭제되거나, 에러 발생");

            String checkCourseSql = "SELECT count(*) FROM open_course WHERE professor_id = 'PROF001'";
            printCount(conn, checkCourseSql, "삭제 전 강의 수");

            String deleteProfSql = "DELETE FROM professor WHERE professor_id = 'PROF001'";
            try {
                PreparedStatement pstmt = conn.prepareStatement(deleteProfSql);
                int rows = pstmt.executeUpdate();
                System.out.println("   -> 삭제 성공! (삭제된 행: " + rows + ")");
                printCount(conn, checkCourseSql, "삭제 후 강의 수 (0이어야 함)");
            } catch (Exception e) {
                System.out.println("   -> 삭제 실패: " + e.getMessage());
                System.out.println("      (이유: FK 제약조건에 ON DELETE CASCADE가 없어서 자식 데이터 보호됨)");
            }

            // 2. 학과 삭제 -> 학생/교수 삭제 확인
            System.out.println("\n[Test 2] 학과(CS) 삭제 시도");
            System.out.println("   -> 예상: CS학과의 모든 학생, 교수, 강의가 사라져야 함");

            String checkStudentSql = "SELECT count(*) FROM student WHERE dept_code = 'CS'";
            printCount(conn, checkStudentSql, "삭제 전 CS학과 학생 수");

            String deleteDeptSql = "DELETE FROM department WHERE dept_code = 'CS'";
            try {
                PreparedStatement pstmt = conn.prepareStatement(deleteDeptSql);
                int rows = pstmt.executeUpdate();
                System.out.println("   -> 삭제 성공! (삭제된 행: " + rows + ")");
                printCount(conn, checkStudentSql, "삭제 후 CS학과 학생 수 (0이어야 함)");
            } catch (Exception e) {
                System.out.println("   -> 삭제 실패: " + e.getMessage());
                System.out.println("      (이유: FK 제약조건에 ON DELETE CASCADE가 없어서 자식 데이터 보호됨)");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printCount(Connection conn, String sql, String label) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("      CHECK: " + label + " = " + rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}