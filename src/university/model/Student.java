package university.model;

import java.util.Date;

/**
 * 학생 정보 VO (Value Object)
 */
public class Student {

    private String studentId;        // 학번
    private String nameKr;           // 한글 이름
    private String nameEn;           // 영문 이름
    private String rrn;              // 주민등록번호 (내국인)
    private String passportNo;       // 여권번호 (외국인)
    private String nationality;      // 국적
    private String deptCode;         // 학과 코드
    private String deptName;         // 학과명 (JOIN용)
    private String status;           // 학적 상태 (ENROLLED/LEAVE/WITHDRAWN/GRADUATED)
    private Date admissionDate;      // 입학일
    private String address;          // 주소
    private String phone;            // 전화번호
    private String email;            // 이메일

    // 기본 생성자
    public Student() {
    }

    // 전체 필드 생성자
    public Student(String studentId, String nameKr, String nameEn, String rrn,
                   String passportNo, String nationality, String deptCode,
                   String status, Date admissionDate, String address,
                   String phone, String email) {
        this.studentId = studentId;
        this.nameKr = nameKr;
        this.nameEn = nameEn;
        this.rrn = rrn;
        this.passportNo = passportNo;
        this.nationality = nationality;
        this.deptCode = deptCode;
        this.status = status;
        this.admissionDate = admissionDate;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getNameKr() {
        return nameKr;
    }

    public void setNameKr(String nameKr) {
        this.nameKr = nameKr;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getPassportNo() {
        return passportNo;
    }

    public void setPassportNo(String passportNo) {
        this.passportNo = passportNo;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(Date admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 학적 상태를 한글로 반환
     */
    public String getStatusKorean() {
        switch (status) {
            case "ENROLLED": return "재학";
            case "LEAVE": return "휴학";
            case "WITHDRAWN": return "제적";
            case "GRADUATED": return "졸업";
            default: return status;
        }
    }

    /**
     * 내국인/외국인 여부 판단
     */
    public boolean isKorean() {
        return rrn != null && !rrn.isEmpty();
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", nameKr='" + nameKr + '\'' +
                ", deptCode='" + deptCode + '\'' +
                ", status='" + status + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}