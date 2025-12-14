package university.dao;

import university.config.DBConnection;
import university.model.Professor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 교수 정보 DAO (Data Access Object)
 */
public class ProfessorDAO {

    private Connection conn;

    public ProfessorDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /**
     * 교수 등록
     */
    public boolean insert(Professor professor) {
        String sql = "INSERT INTO professor (professor_id, name_kr, name_en, rrn, passport_no, " +
                "dept_code, position, office_location, office_phone, email, hire_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, professor.getProfessorId());
            pstmt.setString(2, professor.getNameKr());
            pstmt.setString(3, professor.getNameEn());
            pstmt.setString(4, professor.getRrn());
            pstmt.setString(5, professor.getPassportNo());
            pstmt.setString(6, professor.getDeptCode());
            pstmt.setString(7, professor.getPosition());
            pstmt.setString(8, professor.getOfficeLocation());
            pstmt.setString(9, professor.getOfficePhone());
            pstmt.setString(10, professor.getEmail());
            pstmt.setDate(11, professor.getHireDate() != null ?
                    new java.sql.Date(professor.getHireDate().getTime()) : null);

            int result = pstmt.executeUpdate();
            System.out.println("교수 등록 성공: " + professor.getNameKr());
            return result > 0;

        } catch (SQLException e) {
            System.err.println("교수 등록 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 교수 ID로 조회
     */
    public Professor selectById(String professorId) {
        String sql = "SELECT p.*, d.dept_name FROM professor p " +
                "LEFT JOIN department d ON p.dept_code = d.dept_code " +
                "WHERE p.professor_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, professorId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProfessor(rs);
            }

        } catch (SQLException e) {
            System.err.println("교수 조회 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 전체 교수 목록 조회
     */
    public List<Professor> selectAll() {
        List<Professor> list = new ArrayList<>();
        String sql = "SELECT p.*, d.dept_name FROM professor p " +
                "LEFT JOIN department d ON p.dept_code = d.dept_code " +
                "ORDER BY p.professor_id";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToProfessor(rs));
            }

        } catch (SQLException e) {
            System.err.println("교수 목록 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 학과별 교수 목록 조회
     */
    public List<Professor> selectByDept(String deptCode) {
        List<Professor> list = new ArrayList<>();
        String sql = "SELECT p.*, d.dept_name FROM professor p " +
                "LEFT JOIN department d ON p.dept_code = d.dept_code " +
                "WHERE p.dept_code = ? ORDER BY p.professor_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, deptCode);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToProfessor(rs));
            }

        } catch (SQLException e) {
            System.err.println("학과별 교수 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 이름으로 검색
     */
    public List<Professor> searchByName(String keyword) {
        List<Professor> list = new ArrayList<>();
        String sql = "SELECT p.*, d.dept_name FROM professor p " +
                "LEFT JOIN department d ON p.dept_code = d.dept_code " +
                "WHERE p.name_kr LIKE ? OR p.name_en LIKE ? " +
                "ORDER BY p.professor_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword + "%";
            pstmt.setString(1, searchKeyword);
            pstmt.setString(2, searchKeyword);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToProfessor(rs));
            }

        } catch (SQLException e) {
            System.err.println("교수 검색 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 교수 정보 수정
     */
    public boolean update(Professor professor) {
        String sql = "UPDATE professor SET name_kr = ?, name_en = ?, rrn = ?, passport_no = ?, " +
                "dept_code = ?, position = ?, office_location = ?, office_phone = ?, " +
                "email = ?, hire_date = ? WHERE professor_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, professor.getNameKr());
            pstmt.setString(2, professor.getNameEn());
            pstmt.setString(3, professor.getRrn());
            pstmt.setString(4, professor.getPassportNo());
            pstmt.setString(5, professor.getDeptCode());
            pstmt.setString(6, professor.getPosition());
            pstmt.setString(7, professor.getOfficeLocation());
            pstmt.setString(8, professor.getOfficePhone());
            pstmt.setString(9, professor.getEmail());
            pstmt.setDate(10, professor.getHireDate() != null ?
                    new java.sql.Date(professor.getHireDate().getTime()) : null);
            pstmt.setString(11, professor.getProfessorId());

            int result = pstmt.executeUpdate();
            System.out.println("교수 정보 수정 성공: " + professor.getNameKr());
            return result > 0;

        } catch (SQLException e) {
            System.err.println("교수 정보 수정 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 교수 삭제
     */
    public boolean delete(String professorId) {
        String sql = "DELETE FROM professor WHERE professor_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, professorId);

            int result = pstmt.executeUpdate();
            System.out.println("교수 삭제 성공: " + professorId);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("교수 삭제 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * ResultSet을 Professor 객체로 매핑
     */
    private Professor mapResultSetToProfessor(ResultSet rs) throws SQLException {
        Professor professor = new Professor();
        professor.setProfessorId(rs.getString("professor_id"));
        professor.setNameKr(rs.getString("name_kr"));
        professor.setNameEn(rs.getString("name_en"));
        professor.setRrn(rs.getString("rrn"));
        professor.setPassportNo(rs.getString("passport_no"));
        professor.setDeptCode(rs.getString("dept_code"));
        professor.setDeptName(rs.getString("dept_name"));
        professor.setPosition(rs.getString("position"));
        professor.setOfficeLocation(rs.getString("office_location"));
        professor.setOfficePhone(rs.getString("office_phone"));
        professor.setEmail(rs.getString("email"));
        professor.setHireDate(rs.getDate("hire_date"));
        return professor;
    }
}