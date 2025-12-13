package university.scenario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DeadlockDetectionTest {

    // =============================================================
    // ⚠️ [설정] 본인 DB 정보로 수정하세요
    // =============================================================
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String DB_USER = "c##park2";  // <-- 아이디 확인
    private static final String DB_PW   = "1234";      // <-- 비번 확인

    // 테스트용 강좌 ID (이미 확인된 번호)
    private static final int COURSE_ID_1 = 842;
    private static final int COURSE_ID_2 = 843;

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger deadlockCount = new AtomicInteger(0);
    private static final AtomicBoolean deadlockDetected = new AtomicBoolean(false);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================================");
        System.out.println("   [시나리오 12] Deadlock 감지 테스트 (통합 버전)");
        System.out.println("   ※ 로그 메시지가 'Lock 보유 중'으로 나와야 정상 코드입니다.");
        System.out.println("=================================================================\n");

        // 드라이버 로딩 확인
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("오라클 드라이버를 찾을 수 없습니다.");
            return;
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // ---------------------------------------------------------------------
        // Thread A
        // ---------------------------------------------------------------------
        Thread threadA = new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                startLatch.await(); // 대기

                // 1. 직접 연결 생성 (DAO 안 씀)
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PW);
                conn.setAutoCommit(false); // ⭐️ [핵심] 자동 커밋 끔! 트랜잭션 시작

                System.out.println("\n[Thread A] 시작! (트랜잭션 ON)");

                // Step 1: 강좌 1 업데이트
                String sql = "UPDATE open_course SET enrolled_count = enrolled_count + 1 WHERE open_course_id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, COURSE_ID_1);
                pstmt.executeUpdate();
                pstmt.close();

                System.out.println("[Thread A] Step 1 성공: 강좌 1 잡음 (Lock 보유 중... 절대 안 놓음)");

                // 대기 (상대방이 락 걸 시간 줌)
                Thread.sleep(1000);

                // Step 2: 강좌 2 업데이트 시도 -> 여기서 멈춰야 함!
                System.out.println("[Thread A] Step 2: 강좌 2 진입 시도... (여기서 멈춰야 정상!)");

                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, COURSE_ID_2);
                pstmt.executeUpdate(); // ⛔️ 대기하다가 Deadlock 터짐
                pstmt.close();

                // 성공 시
                conn.commit();
                System.out.println("[Thread A] 모든 작업 완료 (Commit 성공)");
                successCount.incrementAndGet();

            } catch (SQLException e) {
                // Deadlock 감지 (ORA-00060)
                if (e.getErrorCode() == 60 || e.getMessage().contains("00060")) {
                    deadlockDetected.set(true);
                    deadlockCount.incrementAndGet();
                    System.err.println("\n🔴🔴🔴 [Thread A] DEADLOCK 터짐! (ORA-00060) 🔴🔴🔴");
                    System.err.println("   -> 오라클이 A를 강제 종료시킴.");
                } else {
                    System.err.println("[Thread A] 일반 에러: " + e.getMessage());
                }
                try { if(conn != null) conn.rollback(); } catch (SQLException ex) {}
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { if(conn != null) conn.close(); } catch (SQLException e) {}
                doneLatch.countDown();
            }
        }, "Thread-A");

        // ---------------------------------------------------------------------
        // Thread B
        // ---------------------------------------------------------------------
        Thread threadB = new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                startLatch.await();

                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PW);
                conn.setAutoCommit(false); // ⭐️ [핵심]

                System.out.println("\n[Thread B] 시작! (트랜잭션 ON)");

                // Step 1: 강좌 2 업데이트 (A와 반대)
                String sql = "UPDATE open_course SET enrolled_count = enrolled_count + 1 WHERE open_course_id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, COURSE_ID_2);
                pstmt.executeUpdate();
                pstmt.close();

                System.out.println("[Thread B] Step 1 성공: 강좌 2 잡음 (Lock 보유 중... 절대 안 놓음)");

                // 대기
                Thread.sleep(1000);

                // Step 2: 강좌 1 업데이트 시도 -> 여기서 멈춰야 함!
                System.out.println("[Thread B] Step 2: 강좌 1 진입 시도... (여기서 멈춰야 정상!)");

                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, COURSE_ID_1);
                pstmt.executeUpdate(); // ⛔️ 대기하다가 Deadlock 터짐
                pstmt.close();

                conn.commit();
                System.out.println("[Thread B] 모든 작업 완료 (Commit 성공)");
                successCount.incrementAndGet();

            } catch (SQLException e) {
                if (e.getErrorCode() == 60 || e.getMessage().contains("00060")) {
                    deadlockDetected.set(true);
                    deadlockCount.incrementAndGet();
                    System.err.println("\n🔴🔴🔴 [Thread B] DEADLOCK 터짐! (ORA-00060) 🔴🔴🔴");
                    System.err.println("   -> 오라클이 B를 강제 종료시킴.");
                } else {
                    System.err.println("[Thread B] 일반 에러: " + e.getMessage());
                }
                try { if(conn != null) conn.rollback(); } catch (SQLException ex) {}
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { if(conn != null) conn.close(); } catch (SQLException e) {}
                doneLatch.countDown();
            }
        }, "Thread-B");

        // 실행
        threadA.start();
        threadB.start();

        System.out.println("준비 완료... 2초 뒤 시작합니다.");
        Thread.sleep(2000);
        System.out.println("▶▶▶ GO! ◀◀◀");
        startLatch.countDown();

        boolean finished = doneLatch.await(15, TimeUnit.SECONDS);

        System.out.println("\n=================================================================");
        System.out.println("   최종 결과");
        System.out.println("=================================================================");

        if (deadlockDetected.get()) {
            System.out.println("🎉 [성공] Deadlock(ORA-00060)이 발생했습니다!");
            System.out.println("   -> 교착 상태가 제대로 재현되었습니다.");
        } else if (!finished) {
            System.out.println("⚠️ [타임아웃] 스레드가 무한 대기 중입니다. (Deadlock이지만 감지가 늦을 수 있음)");
        } else {
            System.out.println("❌ [실패] Deadlock이 발생하지 않았습니다. 데이터 상태를 확인하세요.");
        }
    }
}