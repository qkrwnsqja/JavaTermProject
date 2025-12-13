package university.scenario;

import university.dao.*;
import university.model.*;

import java.sql.*;

/**
 * [시나리오 13] Null 처리 및 예외 복구 테스트
 */
public class NullHandlingAndRecoveryTest {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   [시나리오 13] Null 처리 및 예외 복구 테스트");
        System.out.println("=================================================================");
        System.out.println("목표: Null 안전성 및 예외 처리 검증");
        System.out.println("=================================================================\n");

        int passCount = 0;
        int totalTests = 6;

        // Test 1: NULL 허용 컬럼 처리
        System.out.println("Test 1: NULL 허용 컬럼 처리\n");

        CourseDAO courseDAO = new CourseDAO();
        Course course = new Course();
        course.setCourseCode("TEST_NULL_01");
        course.setCourseNameKr("NULL 테스트 과목");
        course.setCourseNameEn("Null Test Course");
        course.setCredit(3.0);
        course.setCourseType("교양선택");
        course.setRecommendedYear(null); // NULL
        course.setIsDeleted("N");

        try {
            boolean success = courseDAO.insert(course);
            if (success) {
                System.out.println("  ✅ [PASS] NULL 값 정상 처리됨");
                passCount++;
            } else {
                System.out.println("  ❌ [FAIL] NULL 처리 실패");
            }
        } catch (Exception e) {
            System.out.println("  ❌ [FAIL] 예외 발생: " + e.getMessage());
        }

        // Test 2: NOT NULL 제약 위반
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("\nTest 2: NOT NULL 제약 위반 처리\n");

        Course invalidCourse = new Course();
        invalidCourse.setCourseCode("TEST_NULL_02");
        invalidCourse.setCourseNameKr(null); // NOT NULL 위반
        invalidCourse.setCourseNameEn("Invalid");
        invalidCourse.setCredit(3.0);
        invalidCourse.setCourseType("교양");

        try {
            boolean success = courseDAO.insert(invalidCourse);
            if (!success) {
                System.out.println("  ✅ [PASS] NOT NULL 제약 감지됨");
                passCount++;
            } else {
                System.out.println("  ❌ [FAIL] NULL이 허용됨!");
            }
        } catch (Exception e) {
            System.out.println("  ✅ [PASS] 예외로 차단됨");
            passCount++;
        }

        // Test 3: 트랜잭션 롤백
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("\nTest 3: 트랜잭션 중 예외 발생 시 롤백\n");

        Connection conn = null;
        int beforeCount = 0;
        int afterCount = 0;

        try {
            conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521/xe", "c##park2", "1234");
            conn.setAutoCommit(false);

            beforeCount = getEnrollmentCount(conn);
            System.out.println("  시작 전: " + beforeCount);

            // 정상 INSERT
            String sql1 = "INSERT INTO enrollment " +
                    "(enrollment_id, student_id, open_course_id, requested_at, status, created_by) " +
                    "VALUES (seq_enrollment.NEXTVAL, 'TEST_TX_01', 3, SYSTIMESTAMP, 'APPROVED', 'TEST_TX_01')"; // ★ 3으로 변경!

            try (PreparedStatement pstmt = conn.prepareStatement(sql1)) {
                pstmt.executeUpdate();
                System.out.println("  → 첫 번째 INSERT 성공");
            }

            // 의도적 에러
            String sql2 = "INSERT INTO enrollment " +
                    "(enrollment_id, student_id, open_course_id, requested_at, status, created_by) " +
                    "VALUES (seq_enrollment.NEXTVAL, 'NONEXIST999', 3, SYSTIMESTAMP, 'APPROVED', 'NONEXIST999')"; // ★ 3으로 변경!

            try (PreparedStatement pstmt = conn.prepareStatement(sql2)) {
                pstmt.executeUpdate();
                conn.commit();
            }

        } catch (SQLException e) {
            System.out.println("  → 두 번째 INSERT 실패 (예상됨)");

            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("  → 롤백 실행");
                }
            } catch (SQLException ex) {}

            afterCount = getEnrollmentCount(conn);
            System.out.println("  롤백 후: " + afterCount);

            if (beforeCount == afterCount) {
                System.out.println("\n  ✅ [PASS] 트랜잭션 롤백 성공!");
                passCount++;
            } else {
                System.out.println("\n  ❌ [FAIL] 일부 데이터가 커밋됨!");
            }
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        // Test 4: ResultSet NULL 처리
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("\nTest 4: ResultSet NULL 처리\n");

        try {
            Course nullCourse = courseDAO.selectByCode("TEST_NULL_01");

            if (nullCourse != null) {
                Integer year = nullCourse.getRecommendedYear();

                if (year == null) {
                    System.out.println("  ✅ [PASS] NULL 값 안전하게 처리됨");
                    passCount++;
                }
            }
        } catch (NullPointerException e) {
            System.out.println("  ❌ [FAIL] NPE 발생!");
        }

        // Test 5: Connection 에러 복구
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("\nTest 5: Connection 에러 복구\n");

        try {
            String badUrl = "jdbc:oracle:thin:@localhost:9999/xe";
            Connection badConn = DriverManager.getConnection(badUrl, "c##park2", "1234");
            System.out.println("  ❌ [FAIL] 잘못된 연결이 성공함");
        } catch (SQLException e) {
            System.out.println("  ✅ [PASS] 연결 실패 감지됨");
            passCount++;
        }

        // Test 6: finally 블록에서 리소스 정리
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("\nTest 6: finally 블록에서 리소스 정리\n");

        Connection testConn = null;
        boolean connectionClosed = false;

        try {
            testConn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521/xe", "c##park2", "1234");
            throw new SQLException("테스트 에러");
        } catch (SQLException e) {
            System.out.println("  → 예외 발생");
        } finally {
            try {
                if (testConn != null) {
                    testConn.close();
                    connectionClosed = true;
                    System.out.println("  → finally에서 Connection 닫힘");
                }
            } catch (SQLException e) {}
        }

        if (connectionClosed) {
            System.out.println("  ✅ [PASS] 리소스 정리 성공");
            passCount++;
        }

        // 최종 결과
        System.out.println("\n=================================================================");
        System.out.println("   테스트 결과 요약");
        System.out.println("=================================================================");
        System.out.println("총 테스트: " + totalTests);
        System.out.println("통과: " + passCount);
        System.out.println("실패: " + (totalTests - passCount));
        System.out.println("-----------------------------------------------------------------");

        if (passCount == totalTests) {
            System.out.println("✅ [PASS] 모든 Null 처리 및 예외 복구가 올바릅니다!");
            System.out.println("\n💡 Null 안전성 체크리스트:");
            System.out.println("   ✅ NULL 허용 컬럼 처리");
            System.out.println("   ✅ NOT NULL 제약 검증");
            System.out.println("   ✅ 트랜잭션 롤백");
            System.out.println("   ✅ ResultSet NULL 처리");
            System.out.println("   ✅ Connection 에러 복구");
            System.out.println("   ✅ 리소스 정리");
        } else {
            System.out.println("❌ [FAIL] 일부 테스트 실패!");
        }

        System.out.println("=================================================================\n");

        System.out.println("💡 안전한 코드 패턴:\n");
        System.out.println("[패턴 1: try-with-resources]");
        System.out.println("try (Connection conn = getConnection()) {");
        System.out.println("    // 작업");
        System.out.println("} // 자동 close()\n");
        System.out.println("[패턴 2: NULL 안전 조회]");
        System.out.println("Integer year = rs.getInt(\"year\");");
        System.out.println("if (rs.wasNull()) year = null;\n");
        System.out.println("[패턴 3: 예외 복구]");
        System.out.println("try { conn.commit(); }");
        System.out.println("catch (SQLException e) { conn.rollback(); }");
        System.out.println("finally { if (conn != null) conn.close(); }");
    }

    private static int getEnrollmentCount(Connection conn) {
        if (conn == null) return 0;
        try {
            String sql = "SELECT COUNT(*) FROM enrollment";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {}
        return 0;
    }
}