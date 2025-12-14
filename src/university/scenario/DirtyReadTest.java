package university.scenario;

import java.sql.*;
import java.util.concurrent.CountDownLatch;

/**
 *  Dirty Read 방지 검증 테스트

 * 목적:
 * - 트랜잭션 A가 데이터를 변경했지만 아직 커밋하지 않은 상태
 * - 트랜잭션 B가 이 미확정 데이터를 읽을 수 있는지 확인
 * - Oracle은 READ COMMITTED가 기본이므로 Dirty Read 방지됨

 * Dirty Read란?
 * - 트랜잭션 A가 UPDATE → 아직 커밋 안 함
 * - 트랜잭션 B가 변경된 데이터 읽음
 * - 트랜잭션 A가 롤백 → B가 읽은 데이터는 "더러운 데이터"

 * Oracle 격리 수준:
 * - READ UNCOMMITTED: Dirty Read 가능 (Oracle 미지원!)
 * - READ COMMITTED: Dirty Read 방지 (기본값)

 * 실행 전 준비:
 * 1. test_setup.sql 실행
 * 2. COURSE_ID 설정

 * 예상 결과:
 * - Thread B는 Thread A의 미확정 변경사항을 읽지 못함
 * - Thread B는 원래 값(30명)을 읽음
 *
 * @author Park
 * @since 2025-12-05
 */
public class DirtyReadTest {

    private static final int COURSE_ID = 849; // TEST_LEAK
    private static final int ORIGINAL_CAPACITY = 100;
    private static final int MODIFIED_CAPACITY = 999;

    private static int threadBReadValue = 0;
    private static boolean dirtyReadDetected = false;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================================");
        System.out.println(" Dirty Read 방지 검증 테스트");
        System.out.println("=================================================================");
        System.out.println("목표: 미확정 데이터 읽기 방지 확인");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("강좌 ID: " + COURSE_ID);
        System.out.println("원래 정원: " + ORIGINAL_CAPACITY + "명");
        System.out.println("변경할 정원: " + MODIFIED_CAPACITY + "명 (롤백 예정)");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("Thread A: 정원 변경 → 대기 → 롤백");
        System.out.println("Thread B: Thread A가 변경 중일 때 정원 조회");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("예상: Thread B는 원래 값(" + ORIGINAL_CAPACITY + ")을 읽음 (Dirty Read 방지)");
        System.out.println("=================================================================\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch threadAUpdated = new CountDownLatch(1);
        CountDownLatch threadBDone = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // =====================================================================
        // Thread A: UPDATE 후 롤백
        // =====================================================================
        Thread threadA = new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                startLatch.await();

                System.out.println("\n[Thread A] 시작!");

                String url = "jdbc:oracle:thin:@localhost:1521/xe";
                String user = "c##park2";
                String pass = "1234";
                conn = DriverManager.getConnection(url, user, pass);
                conn.setAutoCommit(false);

                // Step 1: 현재 정원 확인
                System.out.println("[Thread A] Step 1: 현재 정원 조회");
                String sqlSelect = "SELECT capacity FROM open_course WHERE open_course_id = ?";
                pstmt = conn.prepareStatement(sqlSelect);
                pstmt.setInt(1, COURSE_ID);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    int currentCapacity = rs.getInt("capacity");
                    System.out.println("[Thread A]    현재 정원: " + currentCapacity + "명");
                }
                rs.close();
                pstmt.close();

                // Step 2: 정원 변경 (아직 커밋 안 함)
                System.out.println("\n[Thread A] Step 2: 정원을 " + MODIFIED_CAPACITY + "명으로 변경");
                String sqlUpdate = "UPDATE open_course SET capacity = ? WHERE open_course_id = ?";
                pstmt = conn.prepareStatement(sqlUpdate);
                pstmt.setInt(1, MODIFIED_CAPACITY);
                pstmt.setInt(2, COURSE_ID);
                int updated = pstmt.executeUpdate();
                pstmt.close();

                if (updated > 0) {
                    System.out.println("[Thread A] 변경 완료 (아직 커밋 안 함)");
                    System.out.println("[Thread A] 현재 트랜잭션 상태: UNCOMMITTED");
                }

                // 변경 완료 신호
                threadAUpdated.countDown();

                // Step 3: 3초 동안 대기 (Thread B가 읽을 시간을 줌)
                System.out.println("\n[Thread A] Step 3: 3초 동안 대기 중");
                System.out.println("[Thread A]    (이 시간 동안 Thread B가 정원을 조회할 것임)");

                for (int i = 1; i <= 3; i++) {
                    Thread.sleep(1000);
                    System.out.println("[Thread A]    " + i + "초 경과");
                }

                // Thread B가 읽을 때까지 대기
                threadBDone.await();

                // Step 4: 롤백 (변경사항 취소)
                System.out.println("\n[Thread A] Step 4: 롤백 시작");
                conn.rollback();
                System.out.println("[Thread A]  롤백 완료!");
                System.out.println("[Thread A]  정원이 원래대로 복구되었음");

                // 최종 확인
                pstmt = conn.prepareStatement(sqlSelect);
                pstmt.setInt(1, COURSE_ID);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    int finalCapacity = rs.getInt("capacity");
                    System.out.println("[Thread A]    최종 정원: " + finalCapacity + "명");
                }

            } catch (Exception e) {
                try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
                System.err.println("[Thread A] 에러: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception e) {}
                try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
                try { if (conn != null) conn.close(); } catch (Exception e) {}
                doneLatch.countDown();
            }
        }, "Thread-A");

        // =====================================================================
        // Thread B: Thread A가 변경 중일 때 조회
        // =====================================================================
        Thread threadB = new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                startLatch.await();

                // Thread A가 UPDATE할 때까지 대기
                threadAUpdated.await();

                System.out.println("\n[Thread B] 시작!");
                System.out.println("[Thread B] Thread A가 정원을 변경했지만 아직 커밋 안 함");

                Thread.sleep(1000); // 1초 대기

                String url = "jdbc:oracle:thin:@localhost:1521/xe";
                String user = "c##park2";
                String pass = "1234";
                conn = DriverManager.getConnection(url, user, pass);
                conn.setAutoCommit(false);
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                System.out.println("\n[Thread B] Step 1: 정원 조회 시도");
                System.out.println("[Thread B]    Thread A의 미확정 변경(" + MODIFIED_CAPACITY +
                        ")이 보이면 Dirty Read!");

                String sqlSelect = "SELECT capacity FROM open_course WHERE open_course_id = ?";
                pstmt = conn.prepareStatement(sqlSelect);
                pstmt.setInt(1, COURSE_ID);
                rs = pstmt.executeQuery();

                if (rs.next()) {
                    threadBReadValue = rs.getInt("capacity");
                    System.out.println("\n[Thread B] 조회 성공!");
                    System.out.println("[Thread B]   읽은 정원: " + threadBReadValue + "명");

                    // Dirty Read 감지
                    if (threadBReadValue == MODIFIED_CAPACITY) {
                        dirtyReadDetected = true;
                        System.err.println("\n[Thread B] DIRTY READ 감지!");
                        System.err.println("   Thread A의 미확정 변경사항을 읽었습니다!");
                        System.err.println("   → 이는 READ UNCOMMITTED 격리 수준에서만 가능");
                        System.err.println("   → Oracle은 READ COMMITTED가 기본이므로 발생 안 함");
                    } else if (threadBReadValue == ORIGINAL_CAPACITY) {
                        System.out.println("\n[Thread B] 올바른 동작!");
                        System.out.println("   Thread A의 미확정 변경(" + MODIFIED_CAPACITY +
                                ")을 읽지 않음");
                        System.out.println("   원래 값(" + ORIGINAL_CAPACITY + ")을 읽음");
                        System.out.println("   → Dirty Read 방지됨 (READ COMMITTED)");
                    }
                }

                conn.commit();
                threadBDone.countDown();

            } catch (Exception e) {
                try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
                System.err.println("[Thread B] 에러: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception e) {}
                try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
                try { if (conn != null) conn.close(); } catch (Exception e) {}
                doneLatch.countDown();
            }
        }, "Thread-B");

        // 스레드 시작
        threadA.start();
        threadB.start();

        System.out.println("두 스레드 준비 완료. 3초 후 시작\n");
        Thread.sleep(3000);

        System.out.println(" 테스트 시작! \n");
        startLatch.countDown();

        doneLatch.await();

        // 결과 출력
        System.out.println("\n=================================================================");
        System.out.println("   테스트 결과");
        System.out.println("=================================================================");
        System.out.println("Thread A 변경값: " + MODIFIED_CAPACITY + "명 (롤백됨)");
        System.out.println("Thread B 읽은값: " + threadBReadValue + "명");
        System.out.println("원래 값: " + ORIGINAL_CAPACITY + "명");
        System.out.println("-----------------------------------------------------------------");

        if (dirtyReadDetected) {
            System.out.println("[FAIL] Dirty Read가 발생했습니다!");
            System.out.println("   → 이는 Oracle에서는 발생하지 않아야 합니다.");
            System.out.println("   → 격리 수준 설정을 확인하세요.");
        } else if (threadBReadValue == ORIGINAL_CAPACITY) {
            System.out.println("[PASS] Dirty Read가 방지되었습니다!");
            System.out.println("   Thread B는 Thread A의 미확정 변경을 읽지 않았습니다.");
            System.out.println("   READ COMMITTED 격리 수준이 올바르게 작동합니다.");
            System.out.println("\nDirty Read란?");
            System.out.println("   - 다른 트랜잭션의 커밋되지 않은 변경사항을 읽는 것");
            System.out.println("   - 읽은 후 원래 트랜잭션이 롤백되면 \"더러운 데이터\"");
            System.out.println("\nOracle의 격리 수준:");
            System.out.println("   - READ UNCOMMITTED: 미지원 (Dirty Read 가능)");
            System.out.println("   - READ COMMITTED: 기본값 (Dirty Read 방지)");
            System.out.println("   - REPEATABLE READ: 미지원");
            System.out.println("   - SERIALIZABLE: 지원 (가장 강력)");
        } else {
            System.out.println("예상치 못한 결과입니다.");
            System.out.println("   → 데이터 상태를 확인하세요.");
        }

        System.out.println("=================================================================\n");
    }
}