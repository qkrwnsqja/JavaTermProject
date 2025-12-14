package university.dao;

import university.config.DBConnection;
import university.model.OpenCourse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 개설강좌 정보 DAO (Data Access Object)
 */
public class OpenCourseDAO {

    private Connection conn;

    public OpenCourseDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /**
     * 개설강좌 등록
     */
    public boolean insert(OpenCourse openCourse) {
        String sql = "INSERT INTO open_course (open_course_id, year, term, course_code, " +
                "section, professor_id, room, capacity, enrolled_count, is_canceled) " +
                "VALUES (seq_open_course.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, openCourse.getYear());
            pstmt.setString(2, openCourse.getTerm());
            pstmt.setString(3, openCourse.getCourseCode());
            pstmt.setString(4, openCourse.getSection());
            pstmt.setString(5, openCourse.getProfessorId());
            pstmt.setString(6, openCourse.getRoom());
            pstmt.setInt(7, openCourse.getCapacity());
            pstmt.setInt(8, openCourse.getEnrolledCount());
            pstmt.setString(9, openCourse.getIsCanceled() != null ?
                    openCourse.getIsCanceled() : "N");

            int result = pstmt.executeUpdate();
            System.out.println("개설강좌 등록 성공");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("개설강좌 등록 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 개설강좌 ID로 조회
     */
    public OpenCourse selectById(int openCourseId) {
        String sql = "SELECT oc.*, c.course_name_kr, c.credit, p.name_kr as professor_name " +
                "FROM open_course oc " +
                "LEFT JOIN course c ON oc.course_code = c.course_code " +
                "LEFT JOIN professor p ON oc.professor_id = p.professor_id " +
                "WHERE oc.open_course_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, openCourseId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToOpenCourse(rs);
            }

        } catch (SQLException e) {
            System.err.println("개설강좌 조회 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 특정 학기의 개설강좌 목록 조회
     */
    public List<OpenCourse> selectByYearAndTerm(int year, String term) {
        List<OpenCourse> list = new ArrayList<>();
        String sql = "SELECT oc.*, c.course_name_kr, c.credit, p.name_kr as professor_name " +
                "FROM open_course oc " +
                "LEFT JOIN course c ON oc.course_code = c.course_code " +
                "LEFT JOIN professor p ON oc.professor_id = p.professor_id " +
                "WHERE oc.year = ? AND oc.term = ? AND oc.is_canceled = 'N' " +
                "ORDER BY oc.course_code, oc.section";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            pstmt.setString(2, term);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToOpenCourse(rs));
            }

        } catch (SQLException e) {
            System.err.println("개설강좌 목록 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 교수별 개설강좌 조회
     */
    public List<OpenCourse> selectByProfessor(String professorId, int year, String term) {
        List<OpenCourse> list = new ArrayList<>();
        String sql = "SELECT oc.*, c.course_name_kr, c.credit, p.name_kr as professor_name " +
                "FROM open_course oc " +
                "LEFT JOIN course c ON oc.course_code = c.course_code " +
                "LEFT JOIN professor p ON oc.professor_id = p.professor_id " +
                "WHERE oc.professor_id = ? AND oc.year = ? AND oc.term = ? " +
                "ORDER BY oc.course_code";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, professorId);
            pstmt.setInt(2, year);
            pstmt.setString(3, term);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToOpenCourse(rs));
            }

        } catch (SQLException e) {
            System.err.println("교수별 개설강좌 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 과목명으로 검색 (특정 학기)
     */
    public List<OpenCourse> searchByCourseName(int year, String term, String keyword) {
        List<OpenCourse> list = new ArrayList<>();
        String sql = "SELECT oc.*, c.course_name_kr, c.credit, p.name_kr as professor_name " +
                "FROM open_course oc " +
                "LEFT JOIN course c ON oc.course_code = c.course_code " +
                "LEFT JOIN professor p ON oc.professor_id = p.professor_id " +
                "WHERE oc.year = ? AND oc.term = ? AND c.course_name_kr LIKE ? " +
                "AND oc.is_canceled = 'N' " +
                "ORDER BY oc.course_code, oc.section";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            pstmt.setString(2, term);
            pstmt.setString(3, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToOpenCourse(rs));
            }

        } catch (SQLException e) {
            System.err.println("개설강좌 검색 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 개설강좌 정보 수정
     */
    public boolean update(OpenCourse openCourse) {
        String sql = "UPDATE open_course SET year = ?, term = ?, course_code = ?, " +
                "section = ?, professor_id = ?, room = ?, capacity = ?, " +
                "is_canceled = ? WHERE open_course_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, openCourse.getYear());
            pstmt.setString(2, openCourse.getTerm());
            pstmt.setString(3, openCourse.getCourseCode());
            pstmt.setString(4, openCourse.getSection());
            pstmt.setString(5, openCourse.getProfessorId());
            pstmt.setString(6, openCourse.getRoom());
            pstmt.setInt(7, openCourse.getCapacity());
            pstmt.setString(8, openCourse.getIsCanceled());
            pstmt.setInt(9, openCourse.getOpenCourseId());

            int result = pstmt.executeUpdate();
            System.out.println("개설강좌 정보 수정 성공");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("개설강좌 정보 수정 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 개설강좌 폐강 처리
     */
    public boolean cancel(int openCourseId) {
        String sql = "UPDATE open_course SET is_canceled = 'Y' WHERE open_course_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, openCourseId);

            int result = pstmt.executeUpdate();
            System.out.println("개설강좌 폐강 처리 성공");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("개설강좌 폐강 처리 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 개설강좌 삭제
     */
    public boolean delete(int openCourseId) {
        String sql = "DELETE FROM open_course WHERE open_course_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, openCourseId);

            int result = pstmt.executeUpdate();
            System.out.println("개설강좌 삭제 성공");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("개설강좌 삭제 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * ResultSet을 OpenCourse 객체로 매핑
     */
    private OpenCourse mapResultSetToOpenCourse(ResultSet rs) throws SQLException {
        OpenCourse oc = new OpenCourse();
        oc.setOpenCourseId(rs.getInt("open_course_id"));
        oc.setYear(rs.getInt("year"));
        oc.setTerm(rs.getString("term"));
        oc.setCourseCode(rs.getString("course_code"));
        oc.setCourseNameKr(rs.getString("course_name_kr"));
        oc.setCredit(rs.getDouble("credit"));
        oc.setSection(rs.getString("section"));
        oc.setProfessorId(rs.getString("professor_id"));
        oc.setProfessorName(rs.getString("professor_name"));
        oc.setRoom(rs.getString("room"));
        oc.setCapacity(rs.getInt("capacity"));
        oc.setEnrolledCount(rs.getInt("enrolled_count"));
        oc.setIsCanceled(rs.getString("is_canceled"));
        return oc;
    }
}