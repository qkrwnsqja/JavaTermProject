package university.model;

import java.util.Date;

/**
 * 교수 정보 VO (Value Object)
 */
public class Professor {

    private String professorId;      // 교수 ID (PK)
    private String nameKr;           // 한글 이름
    private String nameEn;           // 영문 이름
    private String rrn;              // 주민등록번호 (내국인)
    private String passportNo;       // 여권번호 (외국인)
    private String deptCode;         // 학과 코드
    private String deptName;         // 학과명 (JOIN용)
    private String position;         // 직위 (교수/부교수/조교수)
    private String officeLocation;   // 연구실 위치
    private String officePhone;      // 연구실 전화번호
    private String email;            // 이메일
    private Date hireDate;           // 임용일

    // 기본 생성자
    public Professor() {
    }

    // 전체 필드 생성자
    public Professor(String professorId, String nameKr, String nameEn, String rrn,
                     String passportNo, String deptCode, String position,
                     String officeLocation, String officePhone, String email, Date hireDate) {
        this.professorId = professorId;
        this.nameKr = nameKr;
        this.nameEn = nameEn;
        this.rrn = rrn;
        this.passportNo = passportNo;
        this.deptCode = deptCode;
        this.position = position;
        this.officeLocation = officeLocation;
        this.officePhone = officePhone;
        this.email = email;
        this.hireDate = hireDate;
    }

    // Getters and Setters
    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    @Override
    public String toString() {
        return nameKr + " " + position + " (" + professorId + ")";
    }
}