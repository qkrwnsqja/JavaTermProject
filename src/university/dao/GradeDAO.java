package university.dao;

import university.config.DBConnection;
import university.model.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 성적 정보 DAO (Data Access Object)
 */
public class GradeDAO {

    private Connection conn;

    public GradeDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    /**
     * 성적 등록
     */
    public boolean insert(Grade grade) {
        String sql = "INSERT INTO grade (grade_id, enrollment_id, midterm_score, final_score, " +
                "final_grade, grade_point, grade_confirmed, confirmed_at, confirmed_by) " +
                "VALUES (seq_grade.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, grade.getEnrollmentId());

            if (grade.getMidtermScore() != null) {
                pstmt.setDouble(2, grade.getMidtermScore());
            } else {
                pstmt.setNull(2, Types.DOUBLE);
            }

            if (grade.getFinalScore() != null) {
                pstmt.setDouble(3, grade.getFinalScore());
            } else {
                pstmt.setNull(3, Types.DOUBLE);
            }

            pstmt.setString(4, grade.getFinalGrade());

            if (grade.getGradePoint() != null) {
                pstmt.setDouble(5, grade.getGradePoint());
            } else {
                pstmt.setNull(5, Types.DOUBLE);
            }

            pstmt.setString(6, grade.getGradeConfirmed() != null ?
                    grade.getGradeConfirmed() : "N");
            pstmt.setTimestamp(7, grade.getConfirmedAt());
            pstmt.setString(8, grade.getConfirmedBy());

            int result = pstmt.executeUpdate();
            System.out.println("✓ 성적 등록 성공");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("✗ 성적 등록 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 수강신청 ID로 성적 조회
     */
    public Grade selectByEnrollmentId(int enrollmentId) {
        String sql = "SELECT g.*, e.student_id, s.name_kr as student_name, " +
                "c.course_name_kr, c.credit " +
                "FROM grade g " +
                "JOIN enrollment e ON g.enrollment_id = e.enrollment_id " +
                "JOIN student s ON e.student_id = s.student_id " +
                "JOIN open_course oc ON e.open_course_id = oc.open_course_id " +
                "JOIN course c ON oc.course_code = c.course_code " +
                "WHERE g.enrollment_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, enrollmentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToGrade(rs);
            }

        } catch (SQLException e) {
            System.err.println("✗ 성적 조회 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 학생별 성적 목록 조회 (특정 학기)
     */
    public List<Grade> selectByStudent(String studentId, int year, String term) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT g.*, e.student_id, s.name_kr as student_name, " +
                "c.course_name_kr, c.credit " +
                "FROM grade g " +
                "JOIN enrollment e ON g.enrollment_id = e.enrollment_id " +
                "JOIN student s ON e.student_id = s.student_id " +
                "JOIN open_course oc ON e.open_course_id = oc.open_course_id " +
                "JOIN course c ON oc.course_code = c.course_code " +
                "WHERE e.student_id = ? AND oc.year = ? AND oc.term = ? " +
                "ORDER BY c.course_name_kr";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setInt(2, year);
            pstmt.setString(3, term);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ 학생별 성적 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 개설강좌별 성적 목록 조회
     */
    public List<Grade> selectByOpenCourse(int openCourseId) {
        List<Grade> list = new ArrayList<>();
        String sql = "SELECT g.*, e.student_id, s.name_kr as student_name, " +
                "c.course_name_kr, c.credit " +
                "FROM grade g " +
                "JOIN enrollment e ON g.enrollment_id = e.enrollment_id " +
                "JOIN student s ON e.student_id = s.student_id " +
                "JOIN open_course oc ON e.open_course_id = oc.open_course_id " +
                "JOIN course c ON oc.course_code = c.course_code " +
                "WHERE e.open_course_id = ? " +
                "ORDER BY s.student_id";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, openCourseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            System.err.println("✗ 개설강좌별 성적 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 성적 수정 (기본 - 동시성 제어 없음)
     */
    public boolean update(Grade grade) {
        String sql = "UPDATE grade SET midterm_score = ?, final_score = ?, " +
                "final_grade = ?, grade_point = ?, grade_confirmed = ?, " +
                "confirmed_at = ?, confirmed_by = ? " +
                "WHERE grade_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (grade.getMidtermScore() != null) {
                pstmt.setDouble(1, grade.getMidtermScore());
            } else {
                pstmt.setNull(1, Types.DOUBLE);
            }

            if (grade.getFinalScore() != null) {
                pstmt.setDouble(2, grade.getFinalScore());
            } else {
                pstmt.setNull(2, Types.DOUBLE);
            }

            pstmt.setString(3, grade.getFinalGrade());

            if (grade.getGradePoint() != null) {
                pstmt.setDouble(4, grade.getGradePoint());
            } else {
                pstmt.setNull(4, Types.DOUBLE);
            }

            pstmt.setString(5, grade.getGradeConfirmed());
            pstmt.setTimestamp(6, grade.getConfirmedAt());
            pstmt.setString(7, grade.getConfirmedBy());
            pstmt.setInt(8, grade.getGradeId());

            int result = pstmt.executeUpdate();
            System.out.println("✓ 성적 수정 성공");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("✗ 성적 수정 실패: " + e.getMessage());
            return false;
        }
    }

    // =====================================================================
    // ★ [추가] 시나리오 8번 테스트용: 비관적 락(Pessimistic Lock)이 적용된 성적 수정
    // =====================================================================
    public boolean updateFinalScoreWithLock(int gradeId, double newScore, String professorId, long thinkingTime) {
        Connection newConn = null; // 스레드별 독립 트랜잭션을 위해 새 연결 사용
        PreparedStatement pstmtLock = null;
        PreparedStatement pstmtUpdate = null;
        boolean isSuccess = false;

        try {
            // 1. 독립 커넥션 생성 (동시성 테스트를 위해 필수 - 스레드간 Connection 공유 방지)
            String url = "jdbc:oracle:thin:@localhost:1521/xe";
            String user = "c##park2";
            String pass = "1234";
            newConn = DriverManager.getConnection(url, user, pass);

            // 2. 트랜잭션 시작
            newConn.setAutoCommit(false);

            System.out.println("[" + professorId + "] 성적표 조회 및 수정 시도 (Lock 요청)...");

            // 3. [Locking] 해당 성적 행을 잠금 (FOR UPDATE)
            // -> 이 줄에서 다른 스레드는 락이 풀릴 때까지 멈춰서 기다립니다.
            String sqlLock = "SELECT final_score FROM grade WHERE grade_id = ? FOR UPDATE";
            pstmtLock = newConn.prepareStatement(sqlLock);
            pstmtLock.setInt(1, gradeId);
            ResultSet rs = pstmtLock.executeQuery();

            if (rs.next()) {
                double currentScore = rs.getDouble("final_score");
                System.out.println("[" + professorId + "] 락 획득 성공! (현재 점수: " + currentScore + ")");

                // 4. [Thinking Time] 교수가 점수를 고민하는 시간 (동시성 충돌 유도용 딜레이)
                if (thinkingTime > 0) {
                    System.out.println("[" + professorId + "] 점수 입력 중... (" + thinkingTime + "ms 대기)");
                    try { Thread.sleep(thinkingTime); } catch (InterruptedException e) {}
                }

                // 5. [Update] 점수 반영
                String sqlUpdate = "UPDATE grade SET final_score = ?, confirmed_by = ?, confirmed_at = SYSTIMESTAMP WHERE grade_id = ?";
                pstmtUpdate = newConn.prepareStatement(sqlUpdate);
                pstmtUpdate.setDouble(1, newScore);
                pstmtUpdate.setString(2, professorId);
                pstmtUpdate.setInt(3, gradeId);
                pstmtUpdate.executeUpdate();

                // 6. 커밋 (이때 락이 해제됨)
                newConn.commit();
                System.out.println("[" + professorId + "] 수정 완료 및 저장(Commit)! (점수 -> " + newScore + ")");
                isSuccess = true;
            } else {
                System.out.println("[" + professorId + "] 해당 성적 데이터가 없습니다.");
                newConn.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (newConn != null) newConn.rollback(); } catch (SQLException ex) {}
        } finally {
            // 리소스 정리
            try { if (pstmtLock != null) pstmtLock.close(); } catch (Exception e) {}
            try { if (pstmtUpdate != null) pstmtUpdate.close(); } catch (Exception e) {}
            try { if (newConn != null) newConn.close(); } catch (Exception e) {}
        }
        return isSuccess;
    }

    // =====================================================================
    // ★ [추가] 테스트 편의를 위해 가장 최근 성적 ID를 가져오는 헬퍼 메서드
    // =====================================================================
    public int getLastGradeId() {
        String sql = "SELECT MAX(grade_id) FROM grade";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 성적 확정 처리
     */
    public boolean confirmGrade(int gradeId, String confirmedBy) {
        String sql = "UPDATE grade SET grade_confirmed = 'Y', confirmed_at = SYSTIMESTAMP, " +
                "confirmed_by = ? WHERE grade_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, confirmedBy);
            pstmt.setInt(2, gradeId);

            int result = pstmt.executeUpdate();
            System.out.println("✓ 성적 확정 성공");
            return result > 0;

        } catch (SQLException e) {
            System.err.println("✗ 성적 확정 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 학생의 전체 평점 계산
     */
    public double calculateGPA(String studentId) {
        String sql = "SELECT SUM(g.grade_point * c.credit) / SUM(c.credit) as gpa " +
                "FROM grade g " +
                "JOIN enrollment e ON g.enrollment_id = e.enrollment_id " +
                "JOIN open_course oc ON e.open_course_id = oc.open_course_id " +
                "JOIN course c ON oc.course_code = c.course_code " +
                "WHERE e.student_id = ? AND g.grade_confirmed = 'Y' " +
                "AND g.grade_point IS NOT NULL";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("gpa");
            }

        } catch (SQLException e) {
            System.err.println("✗ 평점 계산 실패: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * ResultSet을 Grade 객체로 매핑
     */
    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setGradeId(rs.getInt("grade_id"));
        grade.setEnrollmentId(rs.getInt("enrollment_id"));
        grade.setStudentId(rs.getString("student_id"));
        grade.setStudentName(rs.getString("student_name"));
        grade.setCourseNameKr(rs.getString("course_name_kr"));
        grade.setCredit(rs.getDouble("credit"));

        double midterm = rs.getDouble("midterm_score");
        if (!rs.wasNull()) {
            grade.setMidtermScore(midterm);
        }

        double finalScore = rs.getDouble("final_score");
        if (!rs.wasNull()) {
            grade.setFinalScore(finalScore);
        }

        grade.setFinalGrade(rs.getString("final_grade"));

        double gradePoint = rs.getDouble("grade_point");
        if (!rs.wasNull()) {
            grade.setGradePoint(gradePoint);
        }

        grade.setGradeConfirmed(rs.getString("grade_confirmed"));
        grade.setConfirmedAt(rs.getTimestamp("confirmed_at"));
        grade.setConfirmedBy(rs.getString("confirmed_by"));
        return grade;
    }
}