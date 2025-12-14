package university.scenario;

import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lock Timeout 테스트

 * 목적:
 * - Thread A가 강좌에 5초 동안 락을 보유
 * - Thread B가 3초 타임아웃 설정으로 락 획득 시도
 * - B가 3초 후 타임아웃 에러를 받는지 검증

 * 왜 중요한가?
 * - 현재 코드는 무한정 대기 (FOR UPDATE)
 * - 운영 환경에서 한 트랜잭션이 멈추면 전체 시스템이 멈출 수 있음
 * - 타임아웃 설정으로 응답 시간 보장

 * 실행 전 준비:
 * 1. test_setup.sql 실행
 * 2. COURSE_ID를 TEST_TIMEOUT의 open_course_id로 교체

 * 예상 결과:
 * - Thread A: 락 획득 → 5초 대기 → 커밋
 * - Thread B: 3초 대기 → ORA-30006 (타임아웃) 에러
 *
 * @author Park
 * @since 2025-12-05
 */
public class LockTimeoutTest {

    // 중요: SQL 조회 결과로 실제 ID를 입력하세요
    private static final int COURSE_ID = 844; // TEST_TIMEOUT의 open_course_id

    private static final String STUDENT_A = "TEST00003";
    private static final String STUDENT_B = "TEST00004";

    private static final AtomicBoolean threadASuccess = new AtomicBoolean(false);
    private static final AtomicBoolean threadBTimeout = new AtomicBoolean(false);
    private static final AtomicInteger lockWaitTime = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================================");
        System.out.println(" Lock Timeout 테스트");
        System.out.println("=================================================================");
        System.out.println("목표: 락 대기 시간 제한 검증");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("강좌 ID: " + COURSE_ID + " (정원 1명)");
        System.out.println("Thread A: 5초 동안 락 보유");
        System.out.println("Thread B: 3초 타임아웃 설정으로 시도");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("예상: Thread B가 3초 후 타임아웃 에러 발생");
        System.out.println("=================================================================\n");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // =====================================================================
        // Thread A: 락을 5초 동안 보유
        // =====================================================================
        Thread threadA = new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmtLock = null;
            PreparedStatement pstmtInsert = null;
            ResultSet rs = null;

            try {
                startLatch.await();

                System.out.println("\n[Thread A] 시작!");

                // 독립적인 Connection 생성
                String url = "jdbc:oracle:thin:@localhost:1521/xe";
                String user = "c##park2";
                String pass = "1234";
                conn = DriverManager.getConnection(url, user, pass);
                conn.setAutoCommit(false);

                System.out.println("[Thread A] Step 1: 강좌 정보 조회 및 락 획득 시도");

                // FOR UPDATE: 무한 대기 (타임아웃 없음)
                String sqlLock = "SELECT capacity, enrolled_count, is_canceled " +
                        "FROM open_course WHERE open_course_id = ? FOR UPDATE";

                pstmtLock = conn.prepareStatement(sqlLock);
                pstmtLock.setInt(1, COURSE_ID);
                rs = pstmtLock.executeQuery();

                if (rs.next()) {
                    int capacity = rs.getInt("capacity");
                    int enrolled = rs.getInt("enrolled_count");
                    String isCanceled = rs.getString("is_canceled");

                    System.out.println("[Thread A] 락 획득 성공!");
                    System.out.println("[Thread A] 현재 정원: " + enrolled + "/" + capacity);

                    if (!"Y".equals(isCanceled) && enrolled < capacity) {
                        // 수강신청 INSERT
                        String sqlInsert = "INSERT INTO enrollment " +
                                "(enrollment_id, student_id, open_course_id, " +
                                "requested_at, status, created_by) " +
                                "VALUES (seq_enrollment.NEXTVAL, ?, ?, " +
                                "SYSTIMESTAMP, 'APPROVED', ?)";

                        pstmtInsert = conn.prepareStatement(sqlInsert);
                        pstmtInsert.setString(1, STUDENT_A);
                        pstmtInsert.setInt(2, COURSE_ID);
                        pstmtInsert.setString(3, STUDENT_A);
                        pstmtInsert.executeUpdate();

                        System.out.println("[Thread A] Step 2: 수강신청 완료 (아직 커밋 안 함)");

                        // 5초 동안 락 보유 (Thread B가 대기하게 함)
                        System.out.println("[Thread A] Step 3: 5초 동안 락 보유 중");
                        System.out.println("[Thread A]    (이 시간 동안 Thread B가 대기할 것임)");

                        for (int i = 1; i <= 5; i++) {
                            Thread.sleep(1000);
                            System.out.println("[Thread A]    " + i + "초 경과...");
                        }

                        // 커밋 (락 해제)
                        conn.commit();
                        System.out.println("[Thread A] Step 4: 커밋 완료 (락 해제)");
                        threadASuccess.set(true);
                    } else {
                        conn.rollback();
                        System.out.println("[Thread A] 신청 불가 (폐강 또는 정원 초과)");
                    }
                } else {
                    conn.rollback();
                    System.out.println("[Thread A] 강좌를 찾을 수 없음");
                }

            } catch (Exception e) {
                try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
                System.err.println("[Thread A] 에러 발생: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception e) {}
                try { if (pstmtLock != null) pstmtLock.close(); } catch (Exception e) {}
                try { if (pstmtInsert != null) pstmtInsert.close(); } catch (Exception e) {}
                try { if (conn != null) conn.close(); } catch (Exception e) {}
                doneLatch.countDown();
            }
        }, "Thread-A");

        // =====================================================================
        // Thread B: 3초 타임아웃 설정으로 시도
        // =====================================================================
        Thread threadB = new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmtLock = null;
            ResultSet rs = null;

            try {
                startLatch.await();

                // 0.5초 후 시작 (Thread A가 먼저 락을 잡도록)
                Thread.sleep(500);

                System.out.println("\n[Thread B] 시작! (0.5초 지연)");

                // 독립적인 Connection 생성
                String url = "jdbc:oracle:thin:@localhost:1521/xe";
                String user = "c##park2";
                String pass = "1234";
                conn = DriverManager.getConnection(url, user, pass);
                conn.setAutoCommit(false);

                System.out.println("[Thread B] Step 1: 강좌 정보 조회 시도");
                System.out.println("[Thread B]    (Thread A가 락을 쥐고 있으므로 대기 예상)");

                // 핵심: WAIT 3 (3초 타임아웃)
                String sqlLock = "SELECT capacity, enrolled_count, is_canceled " +
                        "FROM open_course WHERE open_course_id = ? " +
                        "FOR UPDATE WAIT 3";

                pstmtLock = conn.prepareStatement(sqlLock);
                pstmtLock.setInt(1, COURSE_ID);

                long startTime = System.currentTimeMillis();
                System.out.println("[Thread B]    대기 시작 (최대 3초)");

                rs = pstmtLock.executeQuery();

                long waitTime = System.currentTimeMillis() - startTime;
                lockWaitTime.set((int) waitTime);

                // 만약 여기까지 왔다면 락을 획득한 것
                System.out.println("[Thread B] 락 획득 성공! (대기 시간: " + waitTime + "ms)");
                System.out.println("[Thread B]    → Thread A가 이미 커밋했을 가능성 높음");

                conn.rollback();

            } catch (SQLException e) {
                // 타임아웃 에러 감지
                if (e.getErrorCode() == 30006) { // ORA-30006: resource busy; acquire with WAIT timeout expired
                    threadBTimeout.set(true);
                    long waitTime = System.currentTimeMillis() - System.currentTimeMillis();
                    System.err.println("\n [Thread B] TIMEOUT 발생!");
                    System.err.println("   에러 코드: ORA-30006");
                    System.err.println("   메시지: " + e.getMessage());
                    System.err.println("   → 3초 동안 락을 획득하지 못해 타임아웃되었습니다.");
                    System.err.println("   → 이는 정상 동작입니다.");
                } else {
                    System.err.println("[Thread B] 예외 발생: " + e.getMessage());
                    e.printStackTrace();
                }
                try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
            } catch (InterruptedException e) {
                System.err.println("[Thread B] 인터럽트됨");
            } finally {
                try { if (rs != null) rs.close(); } catch (Exception e) {}
                try { if (pstmtLock != null) pstmtLock.close(); } catch (Exception e) {}
                try { if (conn != null) conn.close(); } catch (Exception e) {}
                doneLatch.countDown();
            }
        }, "Thread-B");

        // 스레드 시작
        threadA.start();
        threadB.start();

        System.out.println("두 스레드 준비 완료. 3초 후 동시 시작\n");
        Thread.sleep(3000);

        System.out.println(" 테스트 시작! \n");
        startLatch.countDown();

        // 최대 10초 대기
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);

        if (!finished) {
            System.err.println("\n 경고: 10초 타임아웃! 스레드가 여전히 실행 중입니다.");
            threadA.interrupt();
            threadB.interrupt();
        }

        // 결과 출력
        System.out.println("\n=================================================================");
        System.out.println("   테스트 결과");
        System.out.println("=================================================================");
        System.out.println("Thread A 성공 여부: " + (threadASuccess.get() ? "성공" : "실패"));
        System.out.println("Thread B 타임아웃 발생: " + (threadBTimeout.get() ? "발생" : "미발생"));

        if (lockWaitTime.get() > 0) {
            System.out.println("Thread B 실제 대기 시간: " + lockWaitTime.get() + "ms");
        }

        System.out.println("-----------------------------------------------------------------");

        if (threadBTimeout.get()) {
            System.out.println("[PASS] Lock Timeout이 정상 작동합니다!");
            System.out.println("   Thread B가 3초 후 타임아웃 에러를 받았습니다.");
            System.out.println("\n현재 코드 문제점:");
            System.out.println("   - EnrollmentDAO.applyCourse()는 'FOR UPDATE' 사용");
            System.out.println("   - 타임아웃 설정이 없어 무한정 대기 가능");
            System.out.println("\n개선 방법:");
            System.out.println("   1. FOR UPDATE WAIT 3 (3초 타임아웃)");
            System.out.println("   2. FOR UPDATE NOWAIT (즉시 에러)");
            System.out.println("   3. Statement Timeout 설정 (JDBC)");
        } else if (threadASuccess.get() && lockWaitTime.get() > 4000) {
            System.out.println("[부분 성공] 타임아웃은 안 났지만 오래 대기했습니다.");
            System.out.println("   Thread B가 " + (lockWaitTime.get() / 1000) + "초 동안 대기");
            System.out.println("   → Thread A가 커밋한 후 락을 획득한 것으로 보입니다.");
        } else {
            System.out.println("[FAIL] 예상치 못한 결과입니다.");
            System.out.println("   → 테스트 환경을 확인하고 재실행하세요.");
        }

        System.out.println("=================================================================\n");
    }
}