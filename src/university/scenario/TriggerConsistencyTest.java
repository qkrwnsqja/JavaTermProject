package university.scenario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * [시나리오 11] Trigger 순서 및 일관성 검증
 */
public class TriggerConsistencyTest {

    private static final int COURSE_ID = 2;
    private static final String[] STUDENTS = {
            "TEST00030", "TEST00031", "TEST00032",
            "TEST00033", "TEST00034", "TEST00035"
    };

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   [시나리오 11] Trigger 순서 및 일관성 검증");
        System.out.println("=================================================================\n");

        Connection conn = null;

        try {
            String url = "jdbc:oracle:thin:@localhost:1521/xe";
            String user = "c##park2";
            String pass = "1234";
            conn = DriverManager.getConnection(url, user, pass);
            conn.setAutoCommit(false);

            // Step 1: Trigger 존재 여부 확인
            System.out.println("Step 1: Trigger 존재 여부 확인...\n");
            List<String> triggers = getTriggers(conn);

            if (triggers.isEmpty()) {
                System.out.println("  ⚠️ Trigger가 없습니다!");
            } else {
                System.out.println("  ✅ 발견된 Trigger:");
                for (String trigger : triggers) {
                    System.out.println("     - " + trigger);
                }
            }

            // Step 2: 초기 상태 확인
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("\nStep 2: 초기 상태 확인...\n");

            int initialCount = getEnrolledCount(conn, COURSE_ID);
            int capacity = getCapacity(conn, COURSE_ID);

            System.out.println("  강좌 정원: " + capacity + "명");
            System.out.println("  현재 신청: " + initialCount + "명");

            // Step 3: 학생 5명 신청
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("\nStep 3: 학생 5명 신청 (정원 내)...\n");

            for (int i = 0; i < 5; i++) {
                insertEnrollment(conn, STUDENTS[i], COURSE_ID);
                int currentCount = getEnrolledCount(conn, COURSE_ID);
                System.out.println("  [" + (i+1) + "] " + STUDENTS[i] + " 신청 → enrolled_count: " + currentCount);
            }

            conn.commit();
            System.out.println("\n  ✅ 5명 신청 완료");

            int afterInsertCount = getEnrolledCount(conn, COURSE_ID);

            // Step 4: 정원 초과 신청 시도
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("\nStep 4: 정원 초과 신청 시도 (6번째 학생)...\n");

            int beforeOverCount = getEnrolledCount(conn, COURSE_ID);
            System.out.println("  6번째 학생 " + STUDENTS[5] + " 신청 시도...");

            boolean overSuccess = false;
            try {
                insertEnrollment(conn, STUDENTS[5], COURSE_ID);
                conn.commit();
                overSuccess = true;
                System.out.println("\n  ❌ [FAIL] 정원 초과인데 신청되었습니다!");
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("\n  ✅ [PASS] Trigger에서 에러 발생!");
            }

            int afterOverCount = getEnrolledCount(conn, COURSE_ID);

            // Step 5: 수강 취소 테스트
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("\nStep 5: 수강 취소 테스트...\n");

            Integer enrollmentId = getEnrollmentId(conn, STUDENTS[0], COURSE_ID);

            if (enrollmentId != null) {
                System.out.println("  학생 " + STUDENTS[0] + " 수강 취소...");

                int beforeDelete = getEnrolledCount(conn, COURSE_ID);
                deleteEnrollment(conn, enrollmentId);
                conn.commit();

                int afterDelete = getEnrolledCount(conn, COURSE_ID);
                System.out.println("  취소 전: " + beforeDelete + "명");
                System.out.println("  취소 후: " + afterDelete + "명");

                if (afterDelete == beforeDelete - 1) {
                    System.out.println("\n  ✅ [PASS] Trigger가 enrolled_count를 자동 감소!");
                } else {
                    System.out.println("\n  ❌ [FAIL] enrolled_count가 감소하지 않음!");
                }
            }

            // 최종 결과
            System.out.println("\n=================================================================");
            System.out.println("   테스트 결과 요약");
            System.out.println("=================================================================");

            int finalCount = getEnrolledCount(conn, COURSE_ID);

            System.out.println("초기: " + initialCount + "명");
            System.out.println("5명 신청 후: " + afterInsertCount + "명");
            System.out.println("1명 취소 후: " + finalCount + "명");

            boolean insertTriggerWorks = (afterInsertCount == initialCount + 5);
            boolean deleteTriggerWorks = (finalCount == afterInsertCount - 1);
            boolean capacityCheckWorks = !overSuccess;

            System.out.println("\nINSERT Trigger: " + (insertTriggerWorks ? "✅" : "❌"));
            System.out.println("DELETE Trigger: " + (deleteTriggerWorks ? "✅" : "❌"));
            System.out.println("정원 체크: " + (capacityCheckWorks ? "✅" : "❌"));

            if (insertTriggerWorks && deleteTriggerWorks && capacityCheckWorks) {
                System.out.println("\n✅ [PASS] 모든 Trigger가 올바르게 작동합니다!");
            } else {
                System.out.println("\n❌ [FAIL] 일부 Trigger가 제대로 작동하지 않습니다!");
            }

            System.out.println("=================================================================\n");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }

    private static List<String> getTriggers(Connection conn) throws SQLException {
        List<String> triggers = new ArrayList<>();
        String sql = "SELECT trigger_name FROM user_triggers " +
                "WHERE table_name IN ('ENROLLMENT', 'OPEN_COURSE')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) triggers.add(rs.getString("trigger_name"));
        }
        return triggers;
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

    private static int getCapacity(Connection conn, int courseId) throws SQLException {
        String sql = "SELECT capacity FROM open_course WHERE open_course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("capacity");
            }
        }
        return 0;
    }

    private static void insertEnrollment(Connection conn, String studentId, int courseId) throws SQLException {
        String sql = "INSERT INTO enrollment (enrollment_id, student_id, open_course_id, " +
                "requested_at, status, created_by) " +
                "VALUES (seq_enrollment.NEXTVAL, ?, ?, SYSTIMESTAMP, 'APPROVED', ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setInt(2, courseId);
            pstmt.setString(3, studentId);
            pstmt.executeUpdate();
        }
    }

    private static Integer getEnrollmentId(Connection conn, String studentId, int courseId) throws SQLException {
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

    private static void deleteEnrollment(Connection conn, int enrollmentId) throws SQLException {
        String sql = "DELETE FROM enrollment WHERE enrollment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            pstmt.executeUpdate();
        }
    }
}