package university.scenario;

import java.sql.*;

/**
 * 폐강 후 정원 복구 안됨 검증
 */
public class CanceledCapacityRecoveryTest {

    private static final int COURSE_ID = 884;

    private static final String[] STUDENTS = {"TEST00020", "TEST00021", "TEST00022"};
    private static final String NEW_STUDENT = "TEST00023";

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("폐강 후 정원 복구 안됨 검증");
        System.out.println("=================================================================\n");

        Connection conn = null;

        try {
            String url = "jdbc:oracle:thin:@localhost:1521/xe";
            String user = "c##park2";
            String pass = "1234";

            conn = DriverManager.getConnection(url, user, pass);

            // 트랜잭션 수동 관리를 위해 자동 커밋 끄기
            conn.setAutoCommit(false);

            // Step 1: 초기 상태 확인
            System.out.println("Step 1: 초기 상태 확인...\n");

            int initialCount = getEnrolledCount(conn, COURSE_ID);
            String isCanceled = getCanceledStatus(conn, COURSE_ID);

            System.out.println("  현재 신청 인원: " + initialCount + "명");
            System.out.println("  폐강 여부: " + isCanceled);

            // Step 2: 강좌 폐강 처리
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("\nStep 2: 강좌 폐강 처리\n");

            String sqlCancel = "UPDATE open_course SET is_canceled = 'Y' WHERE open_course_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCancel)) {
                pstmt.setInt(1, COURSE_ID);
                int updated = pstmt.executeUpdate();

                conn.commit();

                if (updated > 0) {
                    System.out.println("강좌 폐강 완료");
                }
            }

            int countAfterCancel = getEnrolledCount(conn, COURSE_ID);
            System.out.println("  폐강 후 신청 인원: " + countAfterCancel + "명");


            // Step 3: 폐강 후 수강 취소 시도
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("\nStep 3: 폐강된 강좌에서 수강 취소 시도\n");

            Integer enrollmentId = findEnrollmentId(conn, STUDENTS[0], COURSE_ID);

            if (enrollmentId != null) {
                System.out.println("  학생 " + STUDENTS[0] + "의 수강 취소 시도");

                String sqlDelete = "DELETE FROM enrollment WHERE enrollment_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlDelete)) {
                    pstmt.setInt(1, enrollmentId);
                    int deleted = pstmt.executeUpdate();

                    conn.commit(); // 수동 커밋

                    if (deleted > 0) {
                        System.out.println("수강 취소 성공");
                    }
                }

                // 트리거 동작 확인 (폐강된 강좌는 인원이 줄면 안 됨 - 가정)
                int countAfterDelete = getEnrolledCount(conn, COURSE_ID);
                System.out.println("\n  취소 후 enrolled_count: " + countAfterDelete + "명");

                // ※ 참고: 만약 DB 트리거가 '폐강 여부 상관없이 인원 차감'으로 되어 있다면 감소할 수도 있음.
                // 여기서는 '폐강된 강좌는 복구되지 않는다'는 시나리오를 가정함.
                if (countAfterDelete == countAfterCancel) {
                    System.out.println("\n enrolled_count가 변하지 않았습니다! (폐강 정책 준수)");
                } else {
                    System.out.println("\n enrolled_count가 감소했습니다. (일반 취소 로직 동작)");
                }
            }


            // Step 4: 폐강된 강좌에 신규 신청 시도
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("\nStep 4: 폐강된 강좌에 신규 신청 시도\n");

            System.out.println("  학생 " + NEW_STUDENT + " 신청 시도");

            String sqlInsert = "INSERT INTO enrollment " +
                    "(enrollment_id, student_id, open_course_id, " +
                    "requested_at, status) " +
                    "VALUES (seq_enrollment.NEXTVAL, ?, ?, SYSTIMESTAMP, 'APPROVED')";

            boolean insertSuccess = false;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                pstmt.setString(1, NEW_STUDENT);
                pstmt.setInt(2, COURSE_ID);
                pstmt.executeUpdate();

                conn.commit();
                insertSuccess = true;
                System.out.println(" [FAIL] 신청이 허용되었습니다!");
            } catch (SQLException e) {
                // 폐강된 강좌에 신청 시 트리거/제약조건 등으로 막혀야 정상
                System.out.println(" [PASS] 신청이 차단되었습니다! (에러: " + e.getMessage() + ")");
                conn.rollback();
            }

            // 최종 결과
            System.out.println("\n=================================================================");
            System.out.println("   테스트 결과 요약");
            System.out.println("=================================================================");

            int finalCount = getEnrolledCount(conn, COURSE_ID);
            System.out.println("초기 신청 인원: " + initialCount + "명");
            System.out.println("최종 인원: " + finalCount + "명");

            // 로직에 따라 성공/실패 기준은 다를 수 있음
            if (!insertSuccess) {
                System.out.println("\n [PASS] 폐강 강좌 신규 신청 차단 성공!");
            } else {
                System.out.println("\n [FAIL] 폐강 강좌 통제 실패!");
            }

            System.out.println("=================================================================\n");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }

    private static int getEnrolledCount(Connection conn, int courseId) throws SQLException {
        String sql = "SELECT enrolled_count FROM open_course WHERE open_course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("enrolled_count");
            }
        }
        return 0;
    }

    private static String getCanceledStatus(Connection conn, int courseId) throws SQLException {
        String sql = "SELECT is_canceled FROM open_course WHERE open_course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("is_canceled");
            }
        }
        return "N";
    }

    private static Integer findEnrollmentId(Connection conn, String studentId, int courseId) throws SQLException {
        String sql = "SELECT enrollment_id FROM enrollment WHERE student_id = ? AND open_course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setInt(2, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("enrollment_id");
            }
        }
        return null;
    }
}