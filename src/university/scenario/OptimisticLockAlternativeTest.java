package university.scenario;

import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [시나리오 10] 동시 UPDATE 충돌 (Optimistic Lock 대안)
 */
public class OptimisticLockAlternativeTest {

    private static final int COURSE_ID = 885;
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger conflictCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================================");
        System.out.println("   [시나리오 10] 동시 UPDATE 충돌 (Optimistic Lock 대안)");
        System.out.println("=================================================================");
        System.out.println("목표: 낙관적 락 방식의 동시성 제어 검증\n");
        System.out.println("준비사항:");
        System.out.println("  ALTER TABLE open_course ADD version NUMBER DEFAULT 1;");
        System.out.println("  UPDATE open_course SET version = 1 WHERE version IS NULL;");
        System.out.println("=================================================================\n");

        if (!checkVersionColumn()) {
            System.err.println("❌ 에러: version 컬럼이 없습니다!");
            return;
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch threadARead = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // Thread A: 먼저 UPDATE 성공
        Thread threadA = new Thread(() -> {
            Connection conn = null;
            try {
                startLatch.await();
                System.out.println("\n[Thread A] 시작!");

                conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/xe", "c##park2", "1234");
                conn.setAutoCommit(false);

                String sqlSelect = "SELECT capacity, version FROM open_course WHERE open_course_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sqlSelect);
                pstmt.setInt(1, COURSE_ID);
                ResultSet rs = pstmt.executeQuery();

                int capacity = 0, version = 0;
                if (rs.next()) {
                    capacity = rs.getInt("capacity");
                    version = rs.getInt("version");
                    System.out.println("[Thread A] 현재 정원: " + capacity + ", 버전: " + version);
                }
                rs.close();
                pstmt.close();

                threadARead.countDown();

                System.out.println("[Thread A] 비즈니스 로직 처리 중... (2초)");
                Thread.sleep(2000);

                System.out.println("[Thread A] 정원 변경 시도 (50 → 60)...");
                String sqlUpdate = "UPDATE open_course SET capacity = ?, version = version + 1 " +
                        "WHERE open_course_id = ? AND version = ?";

                pstmt = conn.prepareStatement(sqlUpdate);
                pstmt.setInt(1, capacity + 10);
                pstmt.setInt(2, COURSE_ID);
                pstmt.setInt(3, version);

                int updated = pstmt.executeUpdate();

                if (updated > 0) {
                    conn.commit();
                    successCount.incrementAndGet();
                    System.out.println("[Thread A] ✅ UPDATE 성공!");
                } else {
                    conn.rollback();
                    conflictCount.incrementAndGet();
                    System.out.println("[Thread A] ❌ UPDATE 실패 (버전 충돌)");
                }

            } catch (Exception e) {
                try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
                e.printStackTrace();
            } finally {
                try { if (conn != null) conn.close(); } catch (Exception e) {}
                doneLatch.countDown();
            }
        });

        // Thread B: 나중에 UPDATE 시도 → 실패
        Thread threadB = new Thread(() -> {
            Connection conn = null;
            try {
                startLatch.await();
                threadARead.await();

                System.out.println("\n[Thread B] 시작!");

                conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/xe", "c##park2", "1234");
                conn.setAutoCommit(false);

                String sqlSelect = "SELECT capacity, version FROM open_course WHERE open_course_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sqlSelect);
                pstmt.setInt(1, COURSE_ID);
                ResultSet rs = pstmt.executeQuery();

                int capacity = 0, version = 0;
                if (rs.next()) {
                    capacity = rs.getInt("capacity");
                    version = rs.getInt("version");
                    System.out.println("[Thread B] 현재 정원: " + capacity + ", 버전: " + version);
                }
                rs.close();
                pstmt.close();

                System.out.println("[Thread B] 비즈니스 로직 처리 중... (3초)");
                Thread.sleep(3000);

                System.out.println("[Thread B] 정원 변경 시도 (50 → 70)...");
                String sqlUpdate = "UPDATE open_course SET capacity = ?, version = version + 1 " +
                        "WHERE open_course_id = ? AND version = ?";

                pstmt = conn.prepareStatement(sqlUpdate);
                pstmt.setInt(1, capacity + 20);
                pstmt.setInt(2, COURSE_ID);
                pstmt.setInt(3, version);

                int updated = pstmt.executeUpdate();

                if (updated > 0) {
                    conn.commit();
                    successCount.incrementAndGet();
                    System.out.println("[Thread B] ✅ UPDATE 성공!");
                } else {
                    conn.rollback();
                    conflictCount.incrementAndGet();
                    System.err.println("\n[Thread B] ❌ UPDATE 실패 (버전 충돌)");
                }

            } catch (Exception e) {
                try { if (conn != null) conn.rollback(); } catch (Exception ex) {}
                e.printStackTrace();
            } finally {
                try { if (conn != null) conn.close(); } catch (Exception e) {}
                doneLatch.countDown();
            }
        });

        threadA.start();
        threadB.start();

        System.out.println("두 스레드 준비 완료. 3초 후 시작...\n");
        Thread.sleep(3000);

        System.out.println("▶▶▶ 테스트 시작! ◀◀◀\n");
        startLatch.countDown();

        doneLatch.await();

        System.out.println("\n=================================================================");
        System.out.println("   테스트 결과");
        System.out.println("=================================================================");
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("충돌 횟수: " + conflictCount.get());

        if (successCount.get() == 1 && conflictCount.get() == 1) {
            System.out.println("\n✅ [PASS] Optimistic Lock이 올바르게 작동합니다!");
        } else {
            System.out.println("\n❌ [FAIL] 예상치 못한 결과입니다.");
        }

        System.out.println("=================================================================\n");
    }

    private static boolean checkVersionColumn() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521/xe", "c##park2", "1234");
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, "C##PARK2", "OPEN_COURSE", "VERSION");
            boolean exists = rs.next();
            conn.close();
            return exists;
        } catch (SQLException e) {
            return false;
        }
    }
}