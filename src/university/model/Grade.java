package university.model;

import java.sql.Timestamp;

/**
 * 성적 정보 VO (Value Object)
 */
public class Grade {

    private int gradeId;             // 성적 ID (PK, Sequence)
    private int enrollmentId;        // 수강신청 ID (UNIQUE, 1:1 관계)
    private String studentId;        // 학생 ID (JOIN용)
    private String studentName;      // 학생명 (JOIN용)
    private String courseNameKr;     // 과목명 (JOIN용)
    private double credit;           // 학점 (JOIN용)
    private Double midtermScore;     // 중간고사 점수
    private Double finalScore;       // 기말고사 점수
    private String finalGrade;       // 최종 성적 (A+, A0, B+, ...)
    private Double gradePoint;       // 평점 (4.5, 4.0, 3.5, ...)
    private String gradeConfirmed;   // 성적 확정 여부 (Y/N)
    private Timestamp confirmedAt;   // 확정일시
    private String confirmedBy;      // 확정자 (교수 ID)

    // 기본 생성자
    public Grade() {
    }

    // 전체 필드 생성자
    public Grade(int gradeId, int enrollmentId, Double midtermScore, Double finalScore,
                 String finalGrade, Double gradePoint, String gradeConfirmed,
                 Timestamp confirmedAt, String confirmedBy) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.midtermScore = midtermScore;
        this.finalScore = finalScore;
        this.finalGrade = finalGrade;
        this.gradePoint = gradePoint;
        this.gradeConfirmed = gradeConfirmed;
        this.confirmedAt = confirmedAt;
        this.confirmedBy = confirmedBy;
    }

    // Getters and Setters
    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

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

    public String getCourseNameKr() {
        return courseNameKr;
    }

    public void setCourseNameKr(String courseNameKr) {
        this.courseNameKr = courseNameKr;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public Double getMidtermScore() {
        return midtermScore;
    }

    public void setMidtermScore(Double midtermScore) {
        this.midtermScore = midtermScore;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }

    public Double getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(Double gradePoint) {
        this.gradePoint = gradePoint;
    }

    public String getGradeConfirmed() {
        return gradeConfirmed;
    }

    public void setGradeConfirmed(String gradeConfirmed) {
        this.gradeConfirmed = gradeConfirmed;
    }

    public Timestamp getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Timestamp confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(String confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    /**
     * 성적 확정 여부 확인
     */
    public boolean isConfirmed() {
        return "Y".equals(gradeConfirmed);
    }

    /**
     * 평균 점수 계산
     */
    public Double getAverageScore() {
        if (midtermScore == null || finalScore == null) {
            return null;
        }
        return (midtermScore + finalScore) / 2.0;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "gradeId=" + gradeId +
                ", enrollmentId=" + enrollmentId +
                ", finalGrade='" + finalGrade + '\'' +
                ", gradePoint=" + gradePoint +
                '}';
    }
}