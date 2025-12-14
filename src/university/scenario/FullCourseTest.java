package university.scenario;

import university.dao.EnrollmentDAO;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FullCourseTest {

    // 설정: 정원 20명 vs 70명 접속
    private static final int THREAD_COUNT = 70;
    private static final int TARGET_COURSE_ID = 99999;

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 정원 축소(20명) 및 과부하 검증 ===");
        System.out.println("참가: " + THREAD_COUNT + "명 (20250001 ~ 20250070)");
        System.out.println("정원: 20명 (DB 설정 확인 필요)");
        System.out.println("---------------------------------------------");

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        EnrollmentDAO dao = new EnrollmentDAO();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            String studentId = "2025" + String.format("%04d", i);

            executorService.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await(); // 대기

                    // applyCourse 호출 (락 적용됨)
                    boolean success = dao.applyCourse(studentId, TARGET_COURSE_ID);

                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        System.out.println("준비 완료 3초 후 시작");
        Thread.sleep(3000);

        startLatch.countDown(); // 동시 시작

        doneLatch.await();
        executorService.shutdown();

        System.out.println("---------------------------------------------");
        System.out.println("총 시도: " + THREAD_COUNT);
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());

        // 검증
        if (successCount.get() == 20 && failCount.get() == 50) {
            System.out.println("[성공] 정원 20명에 정확히 컷!");
        } else if (successCount.get() > 20) {
            System.out.println("[실패] 정원 초과 (" + successCount.get() + "명)");
        } else {
            System.out.println("[체크] 정원 미달 (" + successCount.get() + "명)");
        }
    }
}