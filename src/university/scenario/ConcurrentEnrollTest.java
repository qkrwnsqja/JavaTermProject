package university.scenario;

import university.dao.EnrollmentDAO;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentEnrollTest {

    // 테스트 설정
    private static final int THREAD_COUNT = 100; // 100명 동시 접속
    private static final int TARGET_COURSE_ID = 99999; // 정원 30명짜리 강의 (TEST_CON)

    // 결과 집계용 (스레드 안전한 카운터)
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== [시나리오 1] 동시성 폭격 테스트 시작 (With Pessimistic Lock) ===");
        System.out.println("대상 강의 ID: " + TARGET_COURSE_ID + " (정원 30명)");
        System.out.println("참가 학생 수: " + THREAD_COUNT + "명 (20250001 ~ 20250100)");
        System.out.println("---------------------------------------------");

        // 100개의 스레드 풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // 동시성 제어를 위한 Latch 설정
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT); // 모든 스레드 준비 확인용
        CountDownLatch startLatch = new CountDownLatch(1); // 동시 시작 신호용 (총성)
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT); // 모든 작업 종료 확인용

        EnrollmentDAO dao = new EnrollmentDAO();

        for (int i = 1; i <= THREAD_COUNT; i++) {
            // 학생 ID 생성 (20250001 ~ 20250100)
            String studentId = "2025" + String.format("%04d", i);

            executorService.submit(() -> {
                try {
                    // 1. 준비 완료 신호 보냄
                    readyLatch.countDown();

                    // 2. 시작 신호가 올 때까지 대기 (100명이 여기서 모임)
                    startLatch.await();

                    // 3. === [핵심] 수강신청 시도 ===
                    // insert() 대신, 락(Lock)이 적용된 applyCourse()를 호출합니다.
                    boolean success = dao.applyCourse(studentId, TARGET_COURSE_ID);

                    if (success) {
                        successCount.incrementAndGet();
                        // 성공 로그는 너무 많으면 콘솔이 지저분해지므로 주석 처리하거나 필요시 해제
                        // System.out.println("[성공] 학생 " + studentId + " 신청 완료");
                    } else {
                        failCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("[에러] 학생 " + studentId + ": " + e.getMessage());
                } finally {
                    // 4. 작업 종료 신호
                    doneLatch.countDown();
                }
            });
        }

        // 메인 스레드: 모든 스레드가 준비될 때까지 대기
        readyLatch.await();
        System.out.println("모든 스레드 준비 완료. 3초 후 폭격 시작...");
        Thread.sleep(3000);

        System.out.println("폭격 시작! (Lock 적용됨)");
        startLatch.countDown(); // 땅! (100개 스레드 동시 진입)

        // 모든 스레드가 끝날 때까지 대기
        doneLatch.await();
        executorService.shutdown();

        // 결과 출력
        System.out.println("---------------------------------------------");
        System.out.println("=== 테스트 결과 ===");
        System.out.println("총 시도: " + THREAD_COUNT);
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("---------------------------------------------");

        // 검증 로직
        if (successCount.get() > 30) {
            System.out.println("[실패] 정원(30명) 초과 발생! (동시성 방어 실패)");
            System.out.println("   -> applyCourse 메서드의 트랜잭션/락 설정을 확인하세요.");
        } else if (successCount.get() == 30) {
            System.out.println("[성공] 완벽합니다! 정원 30명에 정확히 맞춰 신청되었습니다.");
            System.out.println("   -> 비관적 락(Pessimistic Lock)이 정상 작동 중입니다.");
        } else {
            System.out.println("[기타] 정원 미달 (" + successCount.get() + "명). 로직이나 데이터를 확인하세요.");
        }
    }
}