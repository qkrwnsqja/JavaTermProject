package university.model;

/**
 * 학과 정보 VO (Value Object)
 */
public class Department {

    private String deptCode;        // 학과 코드 (PK)
    private String deptName;        // 학과명
    private String collegeName;     // 단과대학명
    private String officeLocation;  // 사무실 위치
    private String officePhone;     // 사무실 전화번호

    // 기본 생성자
    public Department() {
    }

    // 전체 필드 생성자
    public Department(String deptCode, String deptName, String collegeName,
                      String officeLocation, String officePhone) {
        this.deptCode = deptCode;
        this.deptName = deptName;
        this.collegeName = collegeName;
        this.officeLocation = officeLocation;
        this.officePhone = officePhone;
    }

    // Getters and Setters
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

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
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

    @Override
    public String toString() {
        return deptName + " (" + deptCode + ")";
    }
}