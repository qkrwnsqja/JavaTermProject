package university.dao;

import university.config.DBConnection;
import university.config.MiniConnectionPool; // ★ 커넥션 풀 임포트
import university.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 학생 정보 DAO (Data Access Object)
 * - SQL Injection 방어 (PreparedStatement 사용)
 * - 성능 최적화 (MiniConnectionPool 사용)
 */
public class StudentDAO {

    // 생성자에서 연결을 미리 맺지 않음 (풀에서 그때그때 빌려 씀)
    public StudentDAO() {
    }

    /**
     * 학생 등록
     */
    public boolean insert(Student student) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO student (student_id, name_kr, name_en, rrn, passport_no, " +
                "nationality, dept_code, status, admission_date, address, phone, email) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            conn = MiniConnectionPool.getConnection(); // ★ 풀에서 대여
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, student.getStudentId());
            pstmt.setString(2, student.getNameKr());
            pstmt.setString(3, student.getNameEn());
            pstmt.setString(4, student.getRrn());
            pstmt.setString(5, student.getPassportNo());
            pstmt.setString(6, student.getNationality());
            pstmt.setString(7, student.getDeptCode());
            pstmt.setString(8, student.getStatus());
            pstmt.setDate(9, student.getAdmissionDate() != null ?
                    new java.sql.Date(student.getAdmissionDate().getTime()) : null);
            pstmt.setString(10, student.getAddress());
            pstmt.setString(11, student.getPhone());
            pstmt.setString(12, student.getEmail());

            int result = pstmt.executeUpdate();
            // System.out.println("✓ 학생 등록 성공: " + student.getNameKr());
            return result > 0;

        } catch (Exception e) {
            System.err.println("✗ 학생 등록 실패: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(null, pstmt, null);
            MiniConnectionPool.releaseConnection(conn); // ★ 풀에 반납
        }
    }

    /**
     * 학번으로 조회
     */
    public Student selectById(String studentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT s.*, d.dept_name FROM student s " +
                "LEFT JOIN department d ON s.dept_code = d.dept_code " +
                "WHERE s.student_id = ?";

        try {
            conn = MiniConnectionPool.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToStudent(rs);
            }
        } catch (Exception e) {
            System.err.println("✗ 학생 조회 실패: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, null);
            MiniConnectionPool.releaseConnection(conn);
        }
        return null;
    }

    /**
     * 전체 학생 목록 조회
     */
    public List<Student> selectAll() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Student> list = new ArrayList<>();
        String sql = "SELECT s.*, d.dept_name FROM student s " +
                "LEFT JOIN department d ON s.dept_code = d.dept_code " +
                "ORDER BY s.student_id";

        try {
            conn = MiniConnectionPool.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToStudent(rs));
            }
        } catch (Exception e) {
            System.err.println("✗ 학생 목록 조회 실패: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, null);
            MiniConnectionPool.releaseConnection(conn);
        }
        return list;
    }

    /**
     * 이름으로 검색 (★ 핵심: SQL Injection 방어 로직 적용)
     */
    public List<Student> searchByName(String keyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Student> list = new ArrayList<>();

        // OR 1=1 공격 등을 막기 위해 반드시 ?(Placeholder) 사용
        String sql = "SELECT s.*, d.dept_name FROM student s " +
                "LEFT JOIN department d ON s.dept_code = d.dept_code " +
                "WHERE s.name_kr LIKE ? OR s.name_en LIKE ? " +
                "ORDER BY s.student_id";

        try {
            conn = MiniConnectionPool.getConnection();
            pstmt = conn.prepareStatement(sql);

            // 검색어에 %를 붙여서 바인딩 (특수문자가 들어와도 문자로 인식됨)
            String searchKeyword = "%" + keyword + "%";
            pstmt.setString(1, searchKeyword);
            pstmt.setString(2, searchKeyword);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToStudent(rs));
            }

        } catch (Exception e) {
            System.err.println("✗ 학생 검색 실패: " + e.getMessage());
        } finally {
            DBConnection.close(rs, pstmt, null);
            MiniConnectionPool.releaseConnection(conn);
        }
        return list;
    }

    /**
     * 학생 정보 수정
     */
    public boolean update(Student student) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "UPDATE student SET name_kr = ?, name_en = ?, rrn = ?, passport_no = ?, " +
                "nationality = ?, dept_code = ?, status = ?, admission_date = ?, " +
                "address = ?, phone = ?, email = ? WHERE student_id = ?";

        try {
            conn = MiniConnectionPool.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, student.getNameKr());
            pstmt.setString(2, student.getNameEn());
            pstmt.setString(3, student.getRrn());
            pstmt.setString(4, student.getPassportNo());
            pstmt.setString(5, student.getNationality());
            pstmt.setString(6, student.getDeptCode());
            pstmt.setString(7, student.getStatus());
            pstmt.setDate(8, student.getAdmissionDate() != null ?
                    new java.sql.Date(student.getAdmissionDate().getTime()) : null);
            pstmt.setString(9, student.getAddress());
            pstmt.setString(10, student.getPhone());
            pstmt.setString(11, student.getEmail());
            pstmt.setString(12, student.getStudentId());

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            System.err.println("✗ 학생 정보 수정 실패: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(null, pstmt, null);
            MiniConnectionPool.releaseConnection(conn);
        }
    }

    /**
     * 학생 삭제
     */
    public boolean delete(String studentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "DELETE FROM student WHERE student_id = ?";

        try {
            conn = MiniConnectionPool.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            System.err.println("✗ 학생 삭제 실패: " + e.getMessage());
            return false;
        } finally {
            DBConnection.close(null, pstmt, null);
            MiniConnectionPool.releaseConnection(conn);
        }
    }

    // ResultSet 매핑 메서드
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getString("student_id"));
        student.setNameKr(rs.getString("name_kr"));
        student.setNameEn(rs.getString("name_en"));
        student.setRrn(rs.getString("rrn"));
        student.setPassportNo(rs.getString("passport_no"));
        student.setNationality(rs.getString("nationality"));
        student.setDeptCode(rs.getString("dept_code"));
        try { student.setDeptName(rs.getString("dept_name")); } catch (Exception e) {}
        student.setStatus(rs.getString("status"));
        student.setAdmissionDate(rs.getDate("admission_date"));
        student.setAddress(rs.getString("address"));
        student.setPhone(rs.getString("phone"));
        student.setEmail(rs.getString("email"));
        return student;
    }
}