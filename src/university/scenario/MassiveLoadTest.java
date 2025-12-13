package university.scenario;

import university.dao.EnrollmentDAO;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MassiveLoadTest {

    // 1,000명 대규모 접속
    private static final int THREAD_COUNT = 1000;
    private static final int TARGET_COURSE_ID = 99999;

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);
    private static final AtomicInteger errorCount = new AtomicInteger(0); // DB 연결 에러 카운트

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== [시나리오 9] 1,000명 대규모 부하 테스트 (Connection 안정화 버전) ===");
        System.out.println("대상: 99999번 강의 (정원 30명)");
        System.out.println("참가자: 1,000명 (20250001 ~ 20251000)");
        System.out.println("전략: 랜덤 딜레이(0~300ms)를 주어 리스너 폭주(ORA-12541) 방지");
        System.out.println("---------------------------------------------");

        // 1,000개의 스레드를 감당할 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        // 각 스레드에서 사용할 DAO 인스턴스 (내부에서 매번 connection 생성함)
        EnrollmentDAO dao = new EnrollmentDAO();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            String studentId = "2025" + String.format("%04d", i);

            executorService.submit(() -> {
                try {
                    readyLatch.countDown(); // 준비 완료 알림
                    startLatch.await(); // 시작 신호 대기

                    // [핵심 수정] 랜덤 딜레이 추가 (0 ~ 300ms)
                    // 1000명이 0.001초 만에 동시에 Socket을 열면 OS/DB가 거부하므로,
                    // 아주 미세하게 분산시켜 연결 안정성을 확보합니다.
                    long sleepTime = (long) (Math.random() * 300);
                    Thread.sleep(sleepTime);

                    // 수강신청 시도 (트랜잭션 & 락 작동)
                    boolean success = dao.applyCourse(studentId, TARGET_COURSE_ID);

                    if (success) {
                        successCount.incrementAndGet();
                        // System.out.println("[성공] " + studentId + " 신청 완료");
                    } else {
                        failCount.incrementAndGet();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    // 연결 에러 등이 발생하면 여기로 옴
                    errorCount.incrementAndGet();
                    System.err.println("[치명적 에러] " + studentId + ": " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        System.out.println("1,000명 준비 완료. 3초 후 진입 시작...");
        Thread.sleep(3000);

        System.out.println("대규모 접속 시작!");
        long attackStart = System.currentTimeMillis();
        startLatch.countDown(); // Start!

        doneLatch.await(); // 모든 스레드 종료 대기
        long attackEnd = System.currentTimeMillis();
        executorService.shutdown();

        System.out.println("---------------------------------------------");
        System.out.println("=== 테스트 종료 ===");
        System.out.println("총 소요 시간: " + (attackEnd - attackStart) + "ms");
        System.out.println("총 시도: " + THREAD_COUNT);
        System.out.println("성공 (수강신청 완료): " + successCount.get());
        System.out.println("실패 (정원초과/거절): " + failCount.get());
        System.out.println("에러 (DB 접속 불가 등): " + errorCount.get());
        System.out.println("---------------------------------------------");

        // 검증 로직
        if (successCount.get() == 30) {
            System.out.println("[PASS] 정원 30명을 정확히 지켰습니다.");
            if (errorCount.get() == 0) {
                System.out.println("   (DB 연결 오류도 없이 아주 깔끔합니다!)");
            } else {
                System.out.println("   (일부 연결 오류가 있었으나 데이터 무결성은 지켜졌습니다)");
            }
        } else {
            System.out.println("[FAIL] 정원 초과 또는 로직 실패 발생! (성공 수: " + successCount.get() + ")");
        }
    }
}