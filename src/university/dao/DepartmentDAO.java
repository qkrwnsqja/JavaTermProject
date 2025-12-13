package university.dao;

import university.config.DBConnection;
import university.model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 학과 정보 DAO (Data Access Object)
 */
public class DepartmentDAO {

    private Connection conn;

    public DepartmentDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /**
     * 학과 등록
     */
    public boolean insert(Department department) {
        String sql = "INSERT INTO department (dept_code, dept_name, college_name, " +
                "office_location, office_phone) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, department.getDeptCode());
            pstmt.setString(2, department.getDeptName());
            pstmt.setString(3, department.getCollegeName());
            pstmt.setString(4, department.getOfficeLocation());
            pstmt.setString(5, department.getOfficePhone());

            int result = pstmt.executeUpdate();
            System.out.println("✓ 학과 등록 성공: " + department.getDeptName());
            return result > 0;

        } catch (SQLException e) {
            System.err.println("✗ 학과 등록 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 학과 코드로 조회
     */
    public Department selectByCode(String deptCode) {
        String sql = "SELECT * FROM department WHERE dept_code = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deptCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToDepartment(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ 학과 조회 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 전체 학과 목록 조회
     */
    public List<Department> selectAll() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM department ORDER BY dept_code";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToDepartment(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ 학과 목록 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 학과명으로 검색
     */
    public List<Department> searchByName(String keyword) {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM department WHERE dept_name LIKE ? ORDER BY dept_code";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToDepartment(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ 학과 검색 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 학과 정보 수정
     */
    public boolean update(Department department) {
        String sql = "UPDATE department SET dept_name = ?, college_name = ?, " +
                "office_location = ?, office_phone = ? WHERE dept_code = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, department.getDeptName());
            pstmt.setString(2, department.getCollegeName());
            pstmt.setString(3, department.getOfficeLocation());
            pstmt.setString(4, department.getOfficePhone());
            pstmt.setString(5, department.getDeptCode());

            int result = pstmt.executeUpdate();
            System.out.println("✓ 학과 정보 수정 성공: " + department.getDeptName());
            return result > 0;

        } catch (SQLException e) {
            System.err.println("✗ 학과 정보 수정 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 학과 삭제
     */
    public boolean delete(String deptCode) {
        String sql = "DELETE FROM department WHERE dept_code = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deptCode);

            int result = pstmt.executeUpdate();
            System.out.println("✓ 학과 삭제 성공: " + deptCode);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("✗ 학과 삭제 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * ResultSet을 Department 객체로 매핑
     */
    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department dept = new Department();
        dept.setDeptCode(rs.getString("dept_code"));
        dept.setDeptName(rs.getString("dept_name"));
        dept.setCollegeName(rs.getString("college_name"));
        dept.setOfficeLocation(rs.getString("office_location"));
        dept.setOfficePhone(rs.getString("office_phone"));
        return dept;
    }
}