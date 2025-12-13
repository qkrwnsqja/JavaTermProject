package university.model;

import java.sql.Timestamp;

/**
 * 수강신청 정보 VO (Value Object)
 */
public class Enrollment {

    private int enrollmentId;        // 수강신청 ID (PK, Sequence)
    private String studentId;        // 학생 ID
    private String studentName;      // 학생명 (JOIN용)
    private int openCourseId;        // 개설강좌 ID
    private String courseNameKr;     // 과목명 (JOIN용)
    private String professorName;    // 교수명 (JOIN용)
    private double credit;           // 학점 (JOIN용)
    private Timestamp requestedAt;   // 신청일시
    private String status;           // 신청 상태 (APPLIED/APPROVED/WAITING/CANCELLED)
    private String isRetake;         // 재수강 여부 (Y/N)
    private String createdBy;        // 생성자
    private Timestamp createdAt;     // 생성일시

    // 기본 생성자
    public Enrollment() {
    }

    // 전체 필드 생성자
    public Enrollment(int enrollmentId, String studentId, int openCourseId,
                      Timestamp requestedAt, String status, String isRetake,
                      String createdBy, Timestamp createdAt) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.openCourseId = openCourseId;
        this.requestedAt = requestedAt;
        this.status = status;
        this.isRetake = isRetake;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getOpenCourseId() {
        return openCourseId;
    }

    public void setOpenCourseId(int openCourseId) {
        this.openCourseId = openCourseId;
    }

    public String getCourseNameKr() {
        return courseNameKr;
    }

    public void setCourseNameKr(String courseNameKr) {
        this.courseNameKr = courseNameKr;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public Timestamp getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Timestamp requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsRetake() {
        return isRetake;
    }

    public void setIsRetake(String isRetake) {
        this.isRetake = isRetake;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 재수강 여부 확인
     */
    public boolean isRetake() {
        return "Y".equals(isRetake);
    }

    /**
     * 신청 상태를 한글로 반환
     */
    public String getStatusKorean() {
        switch (status) {
            case "APPLIED": return "신청";
            case "APPROVED": return "승인";
            case "WAITING": return "대기";
            case "CANCELLED": return "취소";
            default: return status;
        }
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId='" + studentId + '\'' +
                ", openCourseId=" + openCourseId +
                ", status='" + status + '\'' +
                '}';
    }
}