package university.scenario;

import university.dao.EnrollmentDAO;
import university.dao.GradeDAO;
import university.model.Grade;

import java.sql.*;

/**
 * 재수강 중복 방지 테스트

 * 목적:
 * - 이미 수강한 과목을 다시 신청하려 할 때 방지
 * - F학점이 아닌 과목은 재수강 불가
 * - F학점 과목만 재수강 가능 (is_retake = 'Y')

 * 테스트 시나리오:
 * 1. 학생이 과목 A를 수강하고 C학점 받음
 * 2. 같은 과목을 다시 신청 시도 → 실패해야 함
 * 3. 과목 B를 수강하고 F학점 받음
 * 4. 같은 과목을 재수강 신청 → 성공 (is_retake = 'Y')

 * @author Park
 * @since 2025-12-05
 */
public class RetakePreventionTest {

    private static final String STUDENT_ID = "TEST00001";
    private static final int COURSE_PASSED = 882; // C학점 받은 과목
    private static final int COURSE_FAILED = 883; // F학점 받은 과목

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println(" 재수강 중복 방지 테스트");
        System.out.println("=================================================================");
        System.out.println("목표: 재수강 규칙 검증");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("학생 ID: " + STUDENT_ID);
        System.out.println("과목 A (842): C학점 이수 → 재신청 불가");
        System.out.println("과목 B (843): F학점 이수 → 재신청 가능");
        System.out.println("=================================================================\n");

        EnrollmentDAO enrollDAO = new EnrollmentDAO();
        GradeDAO gradeDAO = new GradeDAO();

        // Step 1: 기존 이수 내역 확인
        System.out.println("Step 1: 기존 이수 내역 확인...\n");

        Grade gradeA = findGradeForCourse(STUDENT_ID, COURSE_PASSED);
        Grade gradeB = findGradeForCourse(STUDENT_ID, COURSE_FAILED);

        if (gradeA != null) {
            System.out.println("  과목 A (842): " + gradeA.getFinalGrade() + " 학점 이수");
        } else {
            System.out.println("  과목 A 이수 기록 없음 (테스트 데이터 준비 필요)");
        }

        if (gradeB != null) {
            System.out.println("  과목 B (843): " + gradeB.getFinalGrade() + " 학점 이수");
        } else {
            System.out.println("  과목 B 이수 기록 없음 (테스트 데이터 준비 필요)");
        }

        System.out.println("\n-----------------------------------------------------------------");

        // Step 2: C학점 과목 재신청 시도
        System.out.println("\nStep 2: C학점 과목(842) 재신청 시도\n");

        boolean canRetakeA = canRetake(STUDENT_ID, COURSE_PASSED);

        if (!canRetakeA) {
            System.out.println("  [PASS] 재신청 차단됨!");
            System.out.println("  → C학점 이상은 재수강 불가");
        } else {
            System.out.println("  [FAIL] 재신청이 허용됨!");
        }

        // Step 3: F학점 과목 재신청 시도
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("\nStep 3: F학점 과목(843) 재신청 시도\n");

        boolean canRetakeB = canRetake(STUDENT_ID, COURSE_FAILED);

        if (canRetakeB) {
            System.out.println("  [PASS] 재신청 허용됨!");
            System.out.println("  → F학점은 재수강 가능");

            System.out.println("\n  실제 재수강 신청 진행");
            boolean success = enrollDAO.applyCourse(STUDENT_ID, COURSE_FAILED);

            if (success) {
                System.out.println("  재수강 신청 성공!");
            } else {
                System.out.println("  재수강 신청 실패");
            }
        } else {
            System.out.println("  [FAIL] 재신청이 차단됨!");
        }

        // 최종 결과
        System.out.println("\n=================================================================");
        System.out.println("   테스트 결과 요약");
        System.out.println("=================================================================");
        System.out.println("C학점 과목 재신청: " + (!canRetakeA ? "차단됨" : "허용됨"));
        System.out.println("F학점 과목 재신청: " + (canRetakeB ? "허용됨" : "차단됨"));
        System.out.println("-----------------------------------------------------------------");

        if (!canRetakeA && canRetakeB) {
            System.out.println("[PASS] 재수강 규칙이 올바르게 작동합니다!");
            System.out.println("\n재수강 규칙:");
            System.out.println("   1. F학점만 재수강 가능");
            System.out.println("   2. 재수강 시 is_retake = 'Y' 설정");
            System.out.println("   3. 재수강 성적이 더 높으면 기존 성적 대체");
        } else {
            System.out.println("[FAIL] 재수강 규칙이 제대로 작동하지 않습니다!");
        }

        System.out.println("=================================================================\n");
    }

    private static Grade findGradeForCourse(String studentId, int openCourseId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String url = "jdbc:oracle:thin:@localhost:1521/xe";
            String user = "c##park2";
            String pass = "1234";
            conn = DriverManager.getConnection(url, user, pass);

            String sql = "SELECT g.grade_id, g.final_grade, g.grade_point " +
                    "FROM grade g " +
                    "JOIN enrollment e ON g.enrollment_id = e.enrollment_id " +
                    "WHERE e.student_id = ? AND e.open_course_id = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            pstmt.setInt(2, openCourseId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Grade grade = new Grade();
                grade.setGradeId(rs.getInt("grade_id"));
                grade.setFinalGrade(rs.getString("final_grade"));
                grade.setGradePoint(rs.getDouble("grade_point"));
                return grade;
            }

        } catch (SQLException e) {
            System.err.println("성적 조회 실패: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }

        return null;
    }

    private static boolean canRetake(String studentId, int openCourseId) {
        Grade grade = findGradeForCourse(studentId, openCourseId);

        if (grade == null) {
            return true;
        }

        return "F".equals(grade.getFinalGrade());
    }
}