package university.service;

import university.dao.EnrollmentDAO;
import university.dao.OpenCourseDAO;
import university.dao.StudentDAO;
import university.model.Enrollment;
import university.model.OpenCourse;
import university.model.Student;

import java.sql.Timestamp;

/**
 * 수강신청 비즈니스 로직 서비스
 */
public class EnrollmentService {

    private EnrollmentDAO enrollmentDAO;
    private OpenCourseDAO openCourseDAO;
    private StudentDAO studentDAO;

    // 시스템 설정값 (실제로는 DB에서 조회)
    private static final double MAX_CREDITS = 18.0;
    private static final double MAX_CREDITS_EXCELLENCE = 21.0;
    private static final double EXCELLENCE_GPA = 4.0;

    public EnrollmentService() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.openCourseDAO = new OpenCourseDAO();
        this.studentDAO = new StudentDAO();
    }

    /**
     * 수강신청 처리 (검증 포함)
     */
    public EnrollmentResult enroll(String studentId, int openCourseId, String createdBy) {
        // 1. 학생 존재 확인
        Student student = studentDAO.selectById(studentId);
        if (student == null) {
            return new EnrollmentResult(false, "존재하지 않는 학생입니다.");
        }

        // 2. 학적 상태 확인
        if (!"ENROLLED".equals(student.getStatus())) {
            return new EnrollmentResult(false, "재학 중인 학생만 수강신청이 가능합니다. (현재: " +
                    student.getStatusKorean() + ")");
        }

        // 3. 개설강좌 존재 확인
        OpenCourse openCourse = openCourseDAO.selectById(openCourseId);
        if (openCourse == null) {
            return new EnrollmentResult(false, "존재하지 않는 개설강좌입니다.");
        }

        // 4. 폐강 여부 확인
        if (openCourse.isCanceled()) {
            return new EnrollmentResult(false, "폐강된 강좌입니다.");
        }

        // 5. 정원 확인
        if (!openCourse.canEnroll()) {
            return new EnrollmentResult(false, "수강 정원이 초과되었습니다. (정원: " +
                    openCourse.getCapacity() + "명)");
        }

        // 6. 중복 신청 확인
        if (enrollmentDAO.isDuplicate(studentId, openCourseId)) {
            return new EnrollmentResult(false, "이미 신청한 강좌입니다.");
        }

        // 7. 최대 학점 확인
        double currentCredits = enrollmentDAO.getTotalCredits(studentId,
                openCourse.getYear(),
                openCourse.getTerm());
        double maxAllowed = MAX_CREDITS; // 기본값

        // TODO: 우수자 여부는 이전 학기 성적으로 판단 (현재는 기본값 사용)

        if (currentCredits + openCourse.getCredit() > maxAllowed) {
            return new EnrollmentResult(false,
                    String.format("최대 신청 학점을 초과합니다. (현재: %.1f학점, 신청: %.1f학점, 최대: %.1f학점)",
                            currentCredits, openCourse.getCredit(), maxAllowed));
        }

        // 8. 수강신청 등록
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setOpenCourseId(openCourseId);
        enrollment.setRequestedAt(new Timestamp(System.currentTimeMillis()));
        enrollment.setStatus("APPROVED"); // 즉시 승인
        enrollment.setIsRetake("N");
        enrollment.setCreatedBy(createdBy);

        boolean success = enrollmentDAO.insert(enrollment);

        if (success) {
            return new EnrollmentResult(true, "수강신청이 완료되었습니다.");
        } else {
            return new EnrollmentResult(false, "수강신청 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 수강신청 취소
     */
    public EnrollmentResult cancelEnrollment(int enrollmentId) {
        // 1. 수강신청 존재 확인
        Enrollment enrollment = enrollmentDAO.selectById(enrollmentId);
        if (enrollment == null) {
            return new EnrollmentResult(false, "존재하지 않는 수강신청입니다.");
        }

        // 2. 취소 가능 상태 확인
        if ("CANCELLED".equals(enrollment.getStatus())) {
            return new EnrollmentResult(false, "이미 취소된 수강신청입니다.");
        }

        // 3. 수강신청 삭제
        boolean success = enrollmentDAO.delete(enrollmentId);

        if (success) {
            return new EnrollmentResult(true, "수강신청이 취소되었습니다.");
        } else {
            return new EnrollmentResult(false, "수강신청 취소 중 오류가 발생했습니다.");
        }
    }

    /**
     * 수강신청 상태 변경
     */
    public EnrollmentResult changeStatus(int enrollmentId, String newStatus) {
        boolean success = enrollmentDAO.updateStatus(enrollmentId, newStatus);

        if (success) {
            return new EnrollmentResult(true, "수강신청 상태가 변경되었습니다.");
        } else {
            return new EnrollmentResult(false, "상태 변경 중 오류가 발생했습니다.");
        }
    }

    /**
     * 수강신청 결과를 담는 내부 클래스
     */
    public static class EnrollmentResult {
        private boolean success;
        private String message;

        public EnrollmentResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}