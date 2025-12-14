package university.scenario;

import university.dao.EnrollmentDAO;

public class CancelledCourseTest {

    private static final String STUDENT_ID = "20250001";
    private static final int TARGET_COURSE_ID = 99999; // 폐강된 강의

    public static void main(String[] args) {
        System.out.println("===  폐강된 강좌 신청 방어 테스트 ===");
        System.out.println("강의 ID: " + TARGET_COURSE_ID);
        System.out.println("상태: 폐강 (is_canceled = 'Y')");
        System.out.println("---------------------------------------------");

        EnrollmentDAO dao = new EnrollmentDAO();

        System.out.println("학생 " + STUDENT_ID + " 수강신청 시도");
        boolean success = dao.applyCourse(STUDENT_ID, TARGET_COURSE_ID);

        System.out.println("---------------------------------------------");
        if (!success) {
            System.out.println("[성공] 신청이 거부되었습니다. (폐강 방어 작동)");
        } else {
            System.out.println("[실패] 폐강된 강의인데 신청되어 버렸습니다!");
            System.out.println("   -> DAO의 is_canceled 체크 로직을 확인하세요.");
        }
    }
}