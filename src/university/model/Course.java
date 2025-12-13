package university.model;

/**
 * 과목 마스터 정보 VO (Value Object)
 */
public class Course {

    private String courseCode;       // 과목 코드 (PK)
    private String courseNameKr;     // 한글 과목명
    private String courseNameEn;     // 영문 과목명
    private double credit;           // 학점
    private String courseType;       // 과목 구분 (전공필수/전공선택/교양)
    private Integer recommendedYear; // 권장 학년
    private String isDeleted;        // 폐지 여부 (Y/N)

    // 기본 생성자
    public Course() {
    }

    // 전체 필드 생성자
    public Course(String courseCode, String courseNameKr, String courseNameEn,
                  double credit, String courseType, Integer recommendedYear, String isDeleted) {
        this.courseCode = courseCode;
        this.courseNameKr = courseNameKr;
        this.courseNameEn = courseNameEn;
        this.credit = credit;
        this.courseType = courseType;
        this.recommendedYear = recommendedYear;
        this.isDeleted = isDeleted;
    }

    // Getters and Setters
    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseNameKr() {
        return courseNameKr;
    }

    public void setCourseNameKr(String courseNameKr) {
        this.courseNameKr = courseNameKr;
    }

    public String getCourseNameEn() {
        return courseNameEn;
    }

    public void setCourseNameEn(String courseNameEn) {
        this.courseNameEn = courseNameEn;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public Integer getRecommendedYear() {
        return recommendedYear;
    }

    public void setRecommendedYear(Integer recommendedYear) {
        this.recommendedYear = recommendedYear;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    /**
     * 폐지된 과목인지 확인
     */
    public boolean isDeleted() {
        return "Y".equals(isDeleted);
    }

    @Override
    public String toString() {
        return courseNameKr + " (" + courseCode + ", " + credit + "학점)";
    }
}