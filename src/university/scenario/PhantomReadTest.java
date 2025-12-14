package university.scenario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 *  Phantom Read 검증 테스트

 * 목적:
 * - 같은 트랜잭션 내에서 동일한 쿼리를 2번 실행할 때
 * - 다른 트랜잭션이 중간에 데이터를 INSERT하면
 * - 두 번째 조회에서 새로운 행이 나타나는지 확인

 * Phantom Read란?
 * - 트랜잭션 A가 조회 → 트랜잭션 B가 INSERT → 트랜잭션 A가 재조회
 * - 첫 번째 조회에는 없던 행이 두 번째 조회에 나타남 (유령처럼!)

 * Oracle 격리 수준:
 * - READ COMMITTED (기본): Phantom Read 발생 가능
 * - SERIALIZABLE: Phantom Read 방지

 * 실행 전 준비:
 * 1. test_setup.sql 실행 완료
 * 2. COURSE_ID를 TEST_LEAK의 open_course_id로 설정

 * 예상 결과:
 * - READ COMMITTED: 두 번째 조회에서 추가된 행이 보임
 * - SERIALIZABLE: 두 번째 조회에서도 동일한 결과
 *
 * @author Park
 * @since 2025-12-05
 */
public class PhantomReadTest {

    private static final int COURSE_ID = 849; // TEST_LEAK의 open_course_id
    private static final String STUDENT_READER = "TEST00010";

    private static int firstCount = 0;
    private static int secondCount = 0;
    private static boolean phantomDetected = false;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================================");
        System.out.println(" Phantom Read 검증 테스트");
        System.out.println("=================================================================");
        System.out.println("목표: 트랜잭션 격리 수준에 따른 Phantom Read 발생 여부 확인");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("강좌 ID: " + COURSE_ID);
        System.out.println("격리 수준: READ COMMITTED (Oracle 기본값)");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("Thread A (Reader): 수강생 목록 조회 → 대기 → 재조회");
        System.out.println("Thread B (Writer): 중간에 새 학생 추가 (INSERT)");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("예상: 두 번째 조회에서 새로운 학생이 나타남 (Phantom Read)");
        System.out.println("=================================================================\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch readerFirstDone = new CountDownLatch(1);
        CountDownLatch writerDone = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // =====================================================================
        // Thread A (Reader): 같은 트랜잭션에서 2번 조회
        // =====================================================================
        Thread readerThread = new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                startLatch.await();

                String url = "jdbc:oracle:thin:@localhost:1521/xe";
                String user = "c##park2";
                String pass = "1234";
                conn = DriverManager.getConnection(url, user, pass);

                // 트랜잭션 시작 (READ COMMITTED - Oracle 기본값)
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                System.out.println("\n[Reader] 트랜잭션 시작");
                System.out.println("[Reader] 격리 수준: READ COMMITTED");

                // === 첫 번째 조회 ===
                System.out.println("\n[Reader] Step 1: 첫 번째 조회 시작...");

                String sql = "SELECT e.enrollment_id, e.student_id, s.name_kr " +
                        "FROM enrollment e " +
                        "JOIN student s ON e.student_id = s.student_id " +
                        "WHERE e.open_course_id = ? " +
                        "ORDER BY e.enrollment_id";

                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, COURSE_ID);
                rs = pstmt.executeQuery();

                List<String> firstResult = new ArrayList<>();
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    String studentName = rs.getString("name_kr");
                    firstResult.add(studentId + " - " + studentName);
                }
                firstCount = firstResult.size();

                System.out.println("[Reader] 첫 번째 조회 결과: " + firstCount + "명");
                for (String student : firstResult) {
                    System.out.println("   - " + student);
                }

                rs.close();
                pstmt.close();

                // Writer에게 신호
                readerFirstDone.countDown();

                // Writer가 INSERT할 때까지 대기
                System.out.println("\n[Reader] Step 2: Writer가 데이터 추가할 때까지 대기");
                writerDone.await();

                Thread.sleep(500); // 약간의 대기

                // === 두 번째 조회 (같은 트랜잭션) ===
                System.out.println("\n[Reader] Step 3: 두 번째 조회 시작 (같은 트랜잭션)");

                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, COURSE_ID);
                rs = pstmt.executeQuery();

                List<String> secondResult = new ArrayList<>();
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    String studentName = rs.getString("name_kr");
                    secondResult.add(studentId + " - " + studentName);
                }
                secondCount = secondResult.size();

                System.out.println("[Reader] 두 번째 조회 결과: " + secondCount + "명");
                for (String student : secondResult) {
                    System.out.println("   - " + student);
                }

                // Phantom Read 감지
                if (secondCount > firstCount) {
                    phantomDetected = true;
                    System.err.println("\n[Reader] PHANTOM READ 감지!");
                    System.err.println("   첫 번째: " + firstCount + "명");
                    System.err.println("   두 번째: " + secondCount + "명");
                    System.err.println("   차이: +" + (secondCount - firstCount) + "명 (유령 행!)");

                    // 새로 나타난 행 출력
                    System.err.println("\n   새로 나타난 학생들:");
                    for (String student : secondResult) {
                        if (!firstResult.contains(student)) {
                            System.err.println("   👻 " + student);
                        }
                    }
                } else {
                    System.out.println("\n[Reader] Phantom Read 없음 (두 조회 결과 동일)");
                }

                conn.commit();
                System.out.println("\n[Reader] 트랜잭션 커밋 완료");

            } catch (Exception e) {
                try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
                System.err.println("[Reader] 에러: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception e) {}
                try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
                try { if (conn != null) conn.close(); } catch (Exception e) {}
                doneLatch.countDown();
            }
        }, "Reader-Thread");

        // =====================================================================
        // Thread B (Writer): 중간에 새 학생 추가
        // =====================================================================
        Thread writerThread = new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;

            try {
                startLatch.await();

                // Reader가 첫 번째 조회를 마칠 때까지 대기
                readerFirstDone.await();

                System.out.println("\n[Writer] 시작!");
                System.out.println("[Writer] Reader의 첫 번째 조회가 완료되었음");

                String url = "jdbc:oracle:thin:@localhost:1521/xe";
                String user = "c##park2";
                String pass = "1234";
                conn = DriverManager.getConnection(url, user, pass);
                conn.setAutoCommit(false);

                // 새로운 학생 3명 추가
                System.out.println("\n[Writer] Step 1: 새로운 학생 3명 추가 중");

                String sql = "INSERT INTO enrollment " +
                        "(enrollment_id, student_id, open_course_id, " +
                        "requested_at, status, created_by) " +
                        "VALUES (seq_enrollment.NEXTVAL, ?, ?, " +
                        "SYSTIMESTAMP, 'APPROVED', ?)";

                pstmt = conn.prepareStatement(sql);

                for (int i = 1; i <= 3; i++) {
                    String studentId = "TEST000" + (10 + i); // TEST00011, TEST00012, TEST00013
                    pstmt.setString(1, studentId);
                    pstmt.setInt(2, COURSE_ID);
                    pstmt.setString(3, studentId);
                    pstmt.executeUpdate();
                    System.out.println("   [Writer] " + studentId + " 추가");
                }

                conn.commit();
                System.out.println("[Writer] Step 2: 커밋 완료 (3명 추가됨)");
                System.out.println("[Writer]    → 이제 Reader가 재조회하면 이 학생들이 보일 것임!");

                writerDone.countDown();

            } catch (Exception e) {
                try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
                System.err.println("[Writer] 에러: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
                try { if (conn != null) conn.close(); } catch (Exception e) {}
                doneLatch.countDown();
            }
        }, "Writer-Thread");

        // 스레드 시작
        readerThread.start();
        writerThread.start();

        System.out.println("두 스레드 준비 완료. 3초 후 시작\n");
        Thread.sleep(3000);

        System.out.println(" 테스트 시작! \n");
        startLatch.countDown();

        doneLatch.await();

        // 결과 출력
        System.out.println("\n=================================================================");
        System.out.println("   테스트 결과");
        System.out.println("=================================================================");
        System.out.println("첫 번째 조회: " + firstCount + "명");
        System.out.println("두 번째 조회: " + secondCount + "명");
        System.out.println("차이: " + (secondCount - firstCount) + "명");
        System.out.println("-----------------------------------------------------------------");

        if (phantomDetected) {
            System.out.println("[PASS] Phantom Read가 발생했습니다!");
            System.out.println("   READ COMMITTED 격리 수준에서는 정상적인 동작입니다.");
            System.out.println("\nPhantom Read란?");
            System.out.println("   - 같은 트랜잭션 내에서 같은 쿼리를 2번 실행");
            System.out.println("   - 다른 트랜잭션이 중간에 INSERT");
            System.out.println("   - 두 번째 조회에 없던 행이 나타남 (유령!)");
            System.out.println("\n해결 방법:");
            System.out.println("   1. SERIALIZABLE 격리 수준 사용");
            System.out.println("      conn.setTransactionIsolation(");
            System.out.println("          Connection.TRANSACTION_SERIALIZABLE);");
            System.out.println();
            System.out.println("   2. FOR UPDATE로 범위 락 (Range Lock)");
            System.out.println("      SELECT ... FOR UPDATE");
            System.out.println();
            System.out.println("   3. 애플리케이션 레벨에서 처리");
            System.out.println("      - Snapshot Isolation");
            System.out.println("      - Optimistic Locking");
        } else {
            System.out.println("[FAIL] Phantom Read가 발생하지 않았습니다.");
            System.out.println("   → Writer가 제대로 INSERT했는지 확인하세요.");
            System.out.println("   → 또는 격리 수준이 SERIALIZABLE일 수 있습니다.");
        }

        System.out.println("=================================================================\n");

        // 추가 테스트: SERIALIZABLE 격리 수준
        System.out.println("추가 정보: SERIALIZABLE 격리 수준으로 재테스트하려면");
        System.out.println("   Reader의 격리 수준을 변경하세요:");
        System.out.println("   conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);");
    }
}