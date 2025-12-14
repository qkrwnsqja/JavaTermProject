package university.scenario;

import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Connection Leak 테스트

 * 목적:
 * - Connection을 제대로 닫지 않으면 어떻게 되는지 검증
 * - Connection Pool이 고갈되는 상황 재현
 * - 메모리 누수 감지

 * 왜 중요한가?
 * - 현재 코드는 try-with-resources를 사용하지 않음
 * - 예외 발생 시 Connection이 닫히지 않을 수 있음
 * - 운영 환경에서 점진적으로 Connection이 고갈됨

 * 테스트 시나리오:
 * - 50개 스레드 중 10개는 의도적으로 Connection을 닫지 않음
 * - 나머지 40개 + 추가 10개가 정상 작동하는지 확인

 * 실행 전 준비:
 * 1. test_setup.sql 실행
 * 2. COURSE_ID를 TEST_LEAK의 open_course_id로 교체

 * 예상 결과:
 * - 초반: 정상 작동
 * - 중반: Connection 획득 지연
 * - 후반: Connection 획득 실패 (타임아웃)
 *
 * @author Park
 * @since 2025-12-05
 */
public class ConnectionLeakTest {

    // 중요: SQL 조회 결과로 실제 ID를 입력하세요
    private static final int COURSE_ID = 845; // TEST_LEAK의 open_course_id

    private static final int TOTAL_THREADS = 500;
    private static final int LEAK_COUNT = 10; // 10개는 의도적으로 안 닫음

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);
    private static final AtomicInteger connectionErrorCount = new AtomicInteger(0);
    private static final AtomicInteger leakedConnectionCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================================");
        System.out.println(" Connection Leak 테스트");
        System.out.println("=================================================================");
        System.out.println("목표: Connection을 닫지 않으면 시스템이 어떻게 되는지 검증");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("총 시도: " + TOTAL_THREADS + "회");
        System.out.println("의도적 누수: " + LEAK_COUNT + "개 (close 안 함)");
        System.out.println("정상 종료: " + (TOTAL_THREADS - LEAK_COUNT) + "개");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("예상: Connection Pool 고갈로 인한 에러 발생");
        System.out.println("=================================================================\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(TOTAL_THREADS);

        // Phase 1: 50개 스레드 실행 (10개는 누수)
        System.out.println("Phase 1: 50개 Connection 요청 (10개는 누수 예정)\n");

        for (int i = 1; i <= TOTAL_THREADS; i++) {
            final int index = i;
            final boolean shouldLeak = true;

            new Thread(() -> {
                Connection conn = null;
                PreparedStatement pstmt = null;
                ResultSet rs = null;

                try {
                    startLatch.await();

                    String studentId = "TEST" + String.format("%05d", index + 10);

                    // Connection 획득 시도
                    long startTime = System.currentTimeMillis();
                    System.out.println("[Thread-" + index + "] Connection 요청...");

                    String url = "jdbc:oracle:thin:@localhost:1521/xe";
                    String user = "c##park2";
                    String pass = "1234";

                    // Connection Timeout 설정 (10초)
                    DriverManager.setLoginTimeout(10);
                    conn = DriverManager.getConnection(url, user, pass);

                    long connectTime = System.currentTimeMillis() - startTime;
                    System.out.println("[Thread-" + index + "] Connection 획득 성공 (" + connectTime + "ms)");

                    conn.setAutoCommit(false);

                    // 간단한 쿼리 실행
                    String sql = "SELECT capacity, enrolled_count FROM open_course " +
                            "WHERE open_course_id = ?";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, COURSE_ID);
                    rs = pstmt.executeQuery();

                    if (rs.next()) {
                        int capacity = rs.getInt("capacity");
                        int enrolled = rs.getInt("enrolled_count");
                        System.out.println("[Thread-" + index + "] 쿼리 성공 (정원: " +
                                enrolled + "/" + capacity + ")");
                        successCount.incrementAndGet();
                    }

                    conn.commit();

                    // 핵심: 5번째마다 Connection을 닫지 않음 (의도적 누수)
                    if (shouldLeak) {
                        System.err.println("[Thread-" + index + "] Connection을 닫지 않고 종료 (누수!)");
                        leakedConnectionCount.incrementAndGet();
                        // conn.close() 호출 안 함!
                    } else {
                        // 정상 종료
                        rs.close();
                        pstmt.close();
                        conn.close();
                        System.out.println("[Thread-" + index + "] Connection 정상 반환");
                    }

                } catch (SQLTimeoutException e) {
                    connectionErrorCount.incrementAndGet();
                    System.err.println("[Thread-" + index + "] Connection Timeout!");
                    System.err.println("   → Connection Pool이 고갈되었을 가능성");
                } catch (SQLException e) {
                    failCount.incrementAndGet();
                    System.err.println("[Thread-" + index + "] DB 에러: " + e.getMessage());
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.err.println("[Thread-" + index + "] 예외: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }, "Thread-" + i).start();
        }

        System.out.println("모든 스레드 준비 완료. 3초 후 시작...\n");
        Thread.sleep(3000);

        System.out.println("Phase 1 시작!\n");
        startLatch.countDown();

        // Phase 1 완료 대기
        boolean phase1Done = doneLatch.await(30, TimeUnit.SECONDS);

        if (!phase1Done) {
            System.err.println("\nPhase 1 일부 스레드가 여전히 대기 중입니다.");
        }

        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("Phase 1 완료. 3초 대기 후 Phase 2 시작\n");
        Thread.sleep(3000);

        // Phase 2: 추가로 10개 스레드 실행 (정상 종료)
        System.out.println("Phase 2: 추가로 10개 Connection 요청 (정상 종료 예정)\n");
        System.out.println("   → Connection Pool에 여유가 있는지 확인\n");

        CountDownLatch phase2Latch = new CountDownLatch(10);
        AtomicInteger phase2Success = new AtomicInteger(0);
        AtomicInteger phase2Fail = new AtomicInteger(0);

        for (int i = 1; i <= 10; i++) {
            final int index = i + TOTAL_THREADS;

            new Thread(() -> {
                Connection conn = null;

                try {
                    String studentId = "TEST" + String.format("%05d", index + 10);

                    long startTime = System.currentTimeMillis();
                    System.out.println("[Phase2-" + index + "] Connection 요청");

                    String url = "jdbc:oracle:thin:@localhost:1521/xe";
                    String user = "c##park2";
                    String pass = "1234";

                    DriverManager.setLoginTimeout(10);
                    conn = DriverManager.getConnection(url, user, pass);

                    long connectTime = System.currentTimeMillis() - startTime;
                    System.out.println("[Phase2-" + index + "] Connection 획득 (" +
                            connectTime + "ms)");

                    // 간단한 쿼리
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL");
                    if (rs.next()) {
                        phase2Success.incrementAndGet();
                    }

                    rs.close();
                    stmt.close();
                    conn.close();

                } catch (SQLTimeoutException e) {
                    phase2Fail.incrementAndGet();
                    System.err.println("[Phase2-" + index + "] Timeout (Pool 고갈)");
                } catch (SQLException e) {
                    phase2Fail.incrementAndGet();
                    System.err.println("[Phase2-" + index + "] DB 에러: " + e.getMessage());
                } finally {
                    phase2Latch.countDown();
                }
            }, "Phase2-" + i).start();
        }

        boolean phase2Done = phase2Latch.await(20, TimeUnit.SECONDS);

        if (!phase2Done) {
            System.err.println("\nPhase 2 타임아웃!");
        }

        // 최종 결과
        System.out.println("\n=================================================================");
        System.out.println("   최종 테스트 결과");
        System.out.println("=================================================================");
        System.out.println("[Phase 1 결과]");
        System.out.println("  총 시도: " + TOTAL_THREADS);
        System.out.println("  성공: " + successCount.get());
        System.out.println("  실패: " + failCount.get());
        System.out.println("  Connection 에러: " + connectionErrorCount.get());
        System.out.println("  의도적 누수: " + leakedConnectionCount.get() + "개");
        System.out.println();
        System.out.println("[Phase 2 결과]");
        System.out.println("  추가 시도: 10");
        System.out.println("  성공: " + phase2Success.get());
        System.out.println("  실패: " + phase2Fail.get());
        System.out.println("-----------------------------------------------------------------");

        if (phase2Fail.get() > 0 || connectionErrorCount.get() > 0) {
            System.out.println("[PASS] Connection Leak 문제가 감지되었습니다!");
            System.out.println("   → Phase 2에서 Connection 획득 실패 발생");
            System.out.println("   → 이는 Phase 1에서 닫지 않은 Connection 때문입니다.");
            System.out.println("\n현재 코드 문제점:");
            System.out.println("   1. try-with-resources 미사용");
            System.out.println("   2. 예외 발생 시 Connection이 닫히지 않을 수 있음");
            System.out.println("   3. finally 블록에서 null 체크 후 close 필요");
            System.out.println("\n개선 방법:");
            System.out.println("   // 현재 코드");
            System.out.println("   Connection conn = DriverManager.getConnection(...);");
            System.out.println("   // 작업");
            System.out.println("   conn.close(); // 예외 발생 시 실행 안 됨!");
            System.out.println();
            System.out.println("   // 개선안 1: try-with-resources");
            System.out.println("   try (Connection conn = DriverManager.getConnection(...)) {");
            System.out.println("       // 작업");
            System.out.println("   } // 자동으로 close()");
            System.out.println();
            System.out.println("   // 개선안 2: finally 블록");
            System.out.println("   try {");
            System.out.println("       conn = DriverManager.getConnection(...);");
            System.out.println("   } finally {");
            System.out.println("       if (conn != null) conn.close();");
            System.out.println("   }");
        } else if (phase2Success.get() == 10) {
            System.out.println("[불명확] Phase 2가 모두 성공했습니다.");
            System.out.println("   → Connection Pool이 충분히 크거나");
            System.out.println("   → 누수된 Connection이 자동 회수되었을 수 있습니다.");
            System.out.println("   → LEAK_COUNT를 늘려서 재테스트 권장");
        } else {
            System.out.println("예상치 못한 결과입니다.");
        }

        System.out.println("=================================================================\n");

        System.out.println("추가 확인 방법:");
        System.out.println("   1. Oracle에서 현재 세션 수 확인:");
        System.out.println("      SELECT COUNT(*) FROM v$session WHERE username = 'C##PARK2';");
        System.out.println();
        System.out.println("   2. 활성 Connection 확인:");
        System.out.println("      SELECT sid, serial#, status, program");
        System.out.println("      FROM v$session");
        System.out.println("      WHERE username = 'C##PARK2';");
    }
}