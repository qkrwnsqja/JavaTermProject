package university.dao;

import university.config.DBConnection;
import university.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 과목 정보 DAO (Data Access Object)
 */
public class CourseDAO {

    private Connection conn;

    public CourseDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /**
     * 과목 등록
     */
    public boolean insert(Course course) {
        String sql = "INSERT INTO course (course_code, course_name_kr, course_name_en, " +
                "credit, course_type, recommended_year, is_deleted) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseNameKr());
            pstmt.setString(3, course.getCourseNameEn());
            pstmt.setDouble(4, course.getCredit());
            pstmt.setString(5, course.getCourseType());

            if (course.getRecommendedYear() != null) {
                pstmt.setInt(6, course.getRecommendedYear());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }

            pstmt.setString(7, course.getIsDeleted() != null ? course.getIsDeleted() : "N");

            int result = pstmt.executeUpdate();
            System.out.println("✓ 과목 등록 성공: " + course.getCourseNameKr());
            return result > 0;

        } catch (SQLException e) {
            System.err.println("✗ 과목 등록 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 과목 코드로 조회
     */
    public Course selectByCode(String courseCode) {
        String sql = "SELECT * FROM course WHERE course_code = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseCode);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToCourse(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ 과목 조회 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 전체 과목 목록 조회 (폐지된 과목 제외)
     */
    public List<Course> selectAll() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE is_deleted = 'N' ORDER BY course_code";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToCourse(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ 과목 목록 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 전체 과목 목록 조회 (폐지된 과목 포함)
     */
    public List<Course> selectAllIncludingDeleted() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM course ORDER BY course_code";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToCourse(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ 과목 목록 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 과목 구분으로 조회
     */
    public List<Course> selectByType(String courseType) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE course_type = ? AND is_deleted = 'N' " +
                "ORDER BY course_code";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToCourse(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ 과목 구분별 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 과목명으로 검색
     */
    public List<Course> searchByName(String keyword) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE (course_name_kr LIKE ? OR course_name_en LIKE ?) " +
                "AND is_deleted = 'N' ORDER BY course_code";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchKeyword = "%" + keyword + "%";
            pstmt.setString(1, searchKeyword);
            pstmt.setString(2, searchKeyword);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToCourse(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ 과목 검색 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 과목 정보 수정
     */
    public boolean update(Course course) {
        String sql = "UPDATE course SET course_name_kr = ?, course_name_en = ?, " +
                "credit = ?, course_type = ?, recommended_year = ?, is_deleted = ? " +
                "WHERE course_code = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, course.getCourseNameKr());
            pstmt.setString(2, course.getCourseNameEn());
            pstmt.setDouble(3, course.getCredit());
            pstmt.setString(4, course.getCourseType());

            if (course.getRecommendedYear() != null) {
                pstmt.setInt(5, course.getRecommendedYear());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.setString(6, course.getIsDeleted());
            pstmt.setString(7, course.getCourseCode());

            int result = pstmt.executeUpdate();
            System.out.println("✓ 과목 정보 수정 성공: " + course.getCourseNameKr());
            return result > 0;

        } catch (SQLException e) {
            System.err.println("✗ 과목 정보 수정 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 과목 삭제 (실제로는 is_deleted를 'Y'로 변경)
     */
    public boolean delete(String courseCode) {
        String sql = "UPDATE course SET is_deleted = 'Y' WHERE course_code = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseCode);

            int result = pstmt.executeUpdate();
            System.out.println("✓ 과목 폐지 성공: " + courseCode);
            return result > 0;

        } catch (SQLException e) {
            System.err.println("✗ 과목 폐지 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * ResultSet을 Course 객체로 매핑
     */
    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseCode(rs.getString("course_code"));
        course.setCourseNameKr(rs.getString("course_name_kr"));
        course.setCourseNameEn(rs.getString("course_name_en"));
        course.setCredit(rs.getDouble("credit"));
        course.setCourseType(rs.getString("course_type"));

        int year = rs.getInt("recommended_year");
        if (!rs.wasNull()) {
            course.setRecommendedYear(year);
        }

        course.setIsDeleted(rs.getString("is_deleted"));
        return course;
    }
}