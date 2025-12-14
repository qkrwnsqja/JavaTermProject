package university.scenario;

import university.dao.EnrollmentDAO;
import university.model.Enrollment;

public class CancelRetryTest {

    private static final int TARGET_COURSE_ID = 99999;
    private static final String STUDENT_A = "20250001"; // 먼저 넣을 학생
    private static final String STUDENT_B = "20250002"; // 대기탈 학생

    public static void main(String[] args) throws InterruptedException {
        System.out.println("===  취소 및 정원 복구 테스트  ===");
        System.out.println("조건: 정원 1명 (Student A 선점 -> 취소 -> Student B 진입)");
        System.out.println("---------------------------------------------");

        EnrollmentDAO dao = new EnrollmentDAO();

        // 1. Student A 수강신청
        System.out.println("[Step 1] 학생 A(" + STUDENT_A + ") 수강신청 시도");
        boolean resultA = dao.applyCourse(STUDENT_A, TARGET_COURSE_ID);
        if (resultA) System.out.println("-> 학생 A 신청 성공 (정원 1/1 마감)");
        else {
            System.out.println("-> 학생 A 신청 실패 (테스트 중단)");
            return;
        }

        Thread.sleep(1000);

        // 2. Student B 수강신청 (실패해야 함)
        System.out.println("\n[Step 2] 학생 B(" + STUDENT_B + ") 수강신청 시도 (정원 초과 예상)");
        boolean resultB1 = dao.applyCourse(STUDENT_B, TARGET_COURSE_ID);
        if (!resultB1) System.out.println("-> 학생 B 신청 실패 (정상: 정원 초과)");
        else System.out.println("-> 학생 B 신청 성공 (버그: 정원 초과인데 뚫림)");

        Thread.sleep(1000);

        // 3. Student A 수강 취소
        System.out.println("\n[Step 3] 학생 A(" + STUDENT_A + ") 수강 취소 시도");

        // DAO에 delete 기능이 있지만, 정원 감소(-1) 로직이 트리거에 있는지 확인 필요.
        // 여기서는 delete 메서드 사용 후, 트리거가 없다면 Java 로직 테스트가 어려울 수 있음.
        // 일단 삭제 시도.

        // A의 enrollment_id를 알아야 삭제 가능 -> 조회
        Enrollment enrollA = dao.selectByStudent(STUDENT_A, 2025, "2학기").stream()
                .filter(e -> e.getOpenCourseId() == TARGET_COURSE_ID)
                .findFirst()
                .orElse(null);

        if (enrollA != null) {
            boolean deleteResult = dao.delete(enrollA.getEnrollmentId());
            if (deleteResult) System.out.println("-> 학생 A 취소 완료 (정원 복구되어야 함)");
            else System.out.println("-> 학생 A 취소 실패");
        } else {
            System.out.println("-> 학생 A 신청 내역을 찾을 수 없음");
            return;
        }

        Thread.sleep(1000);

        // 4. Student B 다시 시도 (성공해야 함)
        System.out.println("\n[Step 4] 학생 B(" + STUDENT_B + ") 재시도");
        boolean resultB2 = dao.applyCourse(STUDENT_B, TARGET_COURSE_ID);
        if (resultB2) System.out.println("-> 학생 B 신청 성공! (빈 자리 차지함)");
        else System.out.println("-> 학생 B 신청 실패 (정원 복구 안됨 or 락 문제)");

        System.out.println("---------------------------------------------");
        System.out.println("=== 테스트 종료 ===");
    }
}