package university.model;

/**
 * 개설강좌 정보 VO (Value Object)
 */
public class OpenCourse {

    private int openCourseId;        // 개설강좌 ID (PK, Sequence)
    private int year;                // 학년도
    private String term;             // 학기 (1학기/2학기/여름학기/겨울학기)
    private String courseCode;       // 과목 코드
    private String courseNameKr;     // 과목명 (JOIN용)
    private double credit;           // 학점 (JOIN용)
    private String section;          // 분반
    private String professorId;      // 담당교수 ID
    private String professorName;    // 담당교수명 (JOIN용)
    private String room;             // 강의실
    private int capacity;            // 수강 정원
    private int enrolledCount;       // 현재 신청 인원
    private String isCanceled;       // 폐강 여부 (Y/N)

    // 기본 생성자
    public OpenCourse() {
    }

    // 전체 필드 생성자
    public OpenCourse(int openCourseId, int year, String term, String courseCode,
                      String section, String professorId, String room, int capacity,
                      int enrolledCount, String isCanceled) {
        this.openCourseId = openCourseId;
        this.year = year;
        this.term = term;
        this.courseCode = courseCode;
        this.section = section;
        this.professorId = professorId;
        this.room = room;
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
        this.isCanceled = isCanceled;
    }

    // Getters and Setters
    public int getOpenCourseId() {
        return openCourseId;
    }

    public void setOpenCourseId(int openCourseId) {
        this.openCourseId = openCourseId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

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

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public String getIsCanceled() {
        return isCanceled;
    }

    public void setIsCanceled(String isCanceled) {
        this.isCanceled = isCanceled;
    }

    /**
     * 폐강 여부 확인
     */
    public boolean isCanceled() {
        return "Y".equals(isCanceled);
    }

    /**
     * 수강 신청 가능 여부 확인
     */
    public boolean canEnroll() {
        return !isCanceled() && enrolledCount < capacity;
    }

    /**
     * 잔여 인원
     */
    public int getRemainingCapacity() {
        return capacity - enrolledCount;
    }

    @Override
    public String toString() {
        return year + "-" + term + " " + courseNameKr + " (" + section + "분반)";
    }
}