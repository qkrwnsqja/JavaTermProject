package university.dao;

import university.config.DBConnection;
import university.config.MiniConnectionPool;
import university.model.Enrollment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    // 기존 코드 호환성을 위한 커넥션 (단순 조회용)
    private Connection conn;

    public EnrollmentDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    // =========================================================================
    // 🚀 [New] 성능 최적화된 수강신청 (커넥션 풀 + 비관적 락)
    // =========================================================================
    public boolean applyCourse(String studentId, int openCourseId) {
        Connection newConn = null;
        PreparedStatement pstmtStudentLock = null;
        PreparedStatement pstmtCourseLock = null;
        PreparedStatement pstmtInsert = null;
        ResultSet rs = null;
        boolean isSuccess = false;

        try {
            // 풀에서 연결 대여 (0.001초)
            newConn = MiniConnectionPool.getConnection();
            newConn.setAutoCommit(false);

            // 1. 학생 락 (동시성 제어)
            String sqlStudentLock = "SELECT student_id FROM student WHERE student_id = ? FOR UPDATE";
            pstmtStudentLock = newConn.prepareStatement(sqlStudentLock);
            pstmtStudentLock.setString(1, studentId);
            rs = pstmtStudentLock.executeQuery();

            if (!rs.next()) {
                System.out.println("THREAD: " + studentId + " 실패: 존재하지 않는 학생");
                newConn.rollback();
                return false;
            }
            rs.close();
            pstmtStudentLock.close();

            // 2. 학점 조회 (트랜잭션 내)
            double currentCredits = getCurrentCreditsInTransaction(newConn, studentId);

            // 3. 강좌 정보 조회 및 락
            String sqlCourseLock =
                    "SELECT oc.capacity, oc.enrolled_count, oc.is_canceled, c.credit " +
                            "FROM open_course oc " +
                            "JOIN course c ON oc.course_code = c.course_code " +
                            "WHERE oc.open_course_id = ? FOR UPDATE";

            pstmtCourseLock = newConn.prepareStatement(sqlCourseLock);
            pstmtCourseLock.setInt(1, openCourseId);
            rs = pstmtCourseLock.executeQuery();

            if (rs.next()) {
                int capacity = rs.getInt("capacity");
                int enrolled = rs.getInt("enrolled_count");
                String isCanceled = rs.getString("is_canceled");
                double courseCredit = rs.getDouble("credit");

                if ("Y".equals(isCanceled)) {
                    newConn.rollback();
                    System.out.println("THREAD: " + studentId + " 실패: 폐강된 강좌");
                    return false;
                }

                if (currentCredits + courseCredit > 18.0) {
                    newConn.rollback();
                    // System.out.println("THREAD: " + studentId + " 실패: 학점 초과");
                    return false;
                }

                if (enrolled < capacity) {
                    String sqlInsert = "INSERT INTO enrollment (enrollment_id, student_id, open_course_id, requested_at, status) " +
                            "VALUES (seq_enrollment.NEXTVAL, ?, ?, SYSTIMESTAMP, 'APPROVED')";

                    pstmtInsert = newConn.prepareStatement(sqlInsert);
                    pstmtInsert.setString(1, studentId);
                    pstmtInsert.setInt(2, openCourseId);
                    pstmtInsert.executeUpdate();

                    newConn.commit(); // 커밋 (락 해제)
                    isSuccess = true;
                } else {
                    newConn.rollback();
                    // System.out.println("THREAD: " + studentId + " 실패: 정원 초과");
                }
            } else {
                newConn.rollback();
                System.out.println("THREAD: " + studentId + " 실패: 존재하지 않는 강의 ID");
            }

        } catch (Exception e) {
            try { if (newConn != null) newConn.rollback(); } catch (SQLException ex) {}
            if (e instanceof SQLException && ((SQLException)e).getErrorCode() == 1) {
                System.out.println("THREAD: " + studentId + " 실패: 이미 신청함");
            } else {
                e.printStackTrace();
            }
        } finally {
            DBConnection.close(rs, pstmtStudentLock, pstmtCourseLock, pstmtInsert);
            MiniConnectionPool.releaseConnection(newConn); // ★ 연결 반납
        }

        return isSuccess;
    }

    private double getCurrentCreditsInTransaction(Connection conn, String studentId) throws SQLException {
        String sql = "SELECT SUM(c.credit) " +
                "FROM enrollment e " +
                "JOIN open_course oc ON e.open_course_id = oc.open_course_id " +
                "JOIN course c ON oc.course_code = c.course_code " +
                "WHERE e.student_id = ? " +
                "AND e.status IN ('APPLIED', 'APPROVED', 'CONFIRMED')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // =========================================================================
    // 🛠️ [Restored] 삭제되었던 기존 메서드 복구 (Service 에러 해결용)
    // =========================================================================

    public boolean isDuplicate(String studentId, int openCourseId) {
        String sql = "SELECT COUNT(*) FROM enrollment WHERE student_id = ? AND open_course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setInt(2, openCourseId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insert(Enrollment enrollment) {
        String sql = "INSERT INTO enrollment (enrollment_id, student_id, open_course_id, status, created_by) VALUES (seq_enrollment.NEXTVAL, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, enrollment.getStudentId());
            pstmt.setInt(2, enrollment.getOpenCourseId());
            pstmt.setString(3, enrollment.getStatus());
            pstmt.setString(4, enrollment.getCreatedBy());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Enrollment> selectByStudent(String studentId, int year, String term) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, s.name_kr as student_name, c.course_name_kr, c.credit, p.name_kr as professor_name " +
                "FROM enrollment e " +
                "LEFT JOIN student s ON e.student_id = s.student_id " +
                "LEFT JOIN open_course oc ON e.open_course_id = oc.open_course_id " +
                "LEFT JOIN course c ON oc.course_code = c.course_code " +
                "LEFT JOIN professor p ON oc.professor_id = p.professor_id " +
                "WHERE e.student_id = ? AND oc.year = ? AND oc.term = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setInt(2, year);
            pstmt.setString(3, term);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapResultSetToEnrollment(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Enrollment> selectByOpenCourse(int openCourseId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, s.name_kr as student_name, c.course_name_kr, c.credit, p.name_kr as professor_name " +
                "FROM enrollment e " +
                "LEFT JOIN student s ON e.student_id = s.student_id " +
                "LEFT JOIN open_course oc ON e.open_course_id = oc.open_course_id " +
                "LEFT JOIN course c ON oc.course_code = c.course_code " +
                "LEFT JOIN professor p ON oc.professor_id = p.professor_id " +
                "WHERE e.open_course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, openCourseId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) list.add(mapResultSetToEnrollment(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Enrollment selectById(int enrollmentId) {
        String sql = "SELECT e.*, s.name_kr as student_name, c.course_name_kr, c.credit, p.name_kr as professor_name " +
                "FROM enrollment e " +
                "LEFT JOIN student s ON e.student_id = s.student_id " +
                "LEFT JOIN open_course oc ON e.open_course_id = oc.open_course_id " +
                "LEFT JOIN course c ON oc.course_code = c.course_code " +
                "LEFT JOIN professor p ON oc.professor_id = p.professor_id " +
                "WHERE e.enrollment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToEnrollment(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete(int enrollmentId) {
        String sql = "DELETE FROM enrollment WHERE enrollment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean updateStatus(int enrollmentId, String status) {
        String sql = "UPDATE enrollment SET status = ? WHERE enrollment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, enrollmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public double getTotalCredits(String studentId, int year, String term) {
        String sql = "SELECT SUM(c.credit) FROM enrollment e " +
                "JOIN open_course oc ON e.open_course_id = oc.open_course_id " +
                "JOIN course c ON oc.course_code = c.course_code " +
                "WHERE e.student_id = ? AND oc.year = ? AND oc.term = ? " +
                "AND e.status IN ('APPLIED', 'APPROVED')";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setInt(2, year);
            pstmt.setString(3, term);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Enrollment mapResultSetToEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
        enrollment.setStudentId(rs.getString("student_id"));
        try { enrollment.setStudentName(rs.getString("student_name")); } catch(Exception e) {}
        enrollment.setOpenCourseId(rs.getInt("open_course_id"));
        try { enrollment.setCourseNameKr(rs.getString("course_name_kr")); } catch(Exception e) {}
        try { enrollment.setProfessorName(rs.getString("professor_name")); } catch(Exception e) {}
        try { enrollment.setCredit(rs.getDouble("credit")); } catch(Exception e) {}
        enrollment.setRequestedAt(rs.getTimestamp("requested_at"));
        enrollment.setStatus(rs.getString("status"));
        return enrollment;
    }
}