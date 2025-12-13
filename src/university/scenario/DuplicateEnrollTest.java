package university.scenario;

import university.dao.EnrollmentDAO;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DuplicateEnrollTest {

    private static final int ATTEMPTS = 5; // 5번 연속 클릭
    private static final String STUDENT_ID = "20250001"; // 테스터 학생
    private static final int TARGET_COURSE_ID = 99999; // 타겟 강의

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== [시나리오 2] 중복 신청 방어 테스트 ===");
        System.out.println("학생: " + STUDENT_ID);
        System.out.println("강의: " + TARGET_COURSE_ID);
        System.out.println("시도 횟수: " + ATTEMPTS + "회 (동시 요청)");
        System.out.println("---------------------------------------------");

        ExecutorService executorService = Executors.newFixedThreadPool(ATTEMPTS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(ATTEMPTS);

        EnrollmentDAO dao = new EnrollmentDAO();

        for (int i = 0; i < ATTEMPTS; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 대기

                    // 수강신청 시도
                    boolean success = dao.applyCourse(STUDENT_ID, TARGET_COURSE_ID);

                    if (success) {
                        successCount.incrementAndGet();
                        System.out.println("[성공] 신청 완료");
                    } else {
                        failCount.incrementAndGet();
                        System.out.println("[실패] 신청 거부됨");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        System.out.println("3초 후 동시 클릭...");
        Thread.sleep(3000);
        startLatch.countDown(); // 시작!

        doneLatch.await();
        executorService.shutdown();

        System.out.println("---------------------------------------------");
        System.out.println("총 시도: " + ATTEMPTS);
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());

        if (successCount.get() == 1 && failCount.get() == 4) {
            System.out.println("[성공] 중복 신청이 완벽하게 차단되었습니다.");
        } else {
            System.out.println("[실패] 중복 신청이 허용되었거나 로직 오류가 있습니다.");
        }
    }
}