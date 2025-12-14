package university.scenario;

import university.dao.EnrollmentDAO;
import university.dao.OpenCourseDAO;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 학점 제한 경계값 테스트

 * 목적:
 * - 최대 학점(18학점) 경계에서 수강신청이 올바르게 제어되는지 검증
 * - 17.5학점 신청 상태에서 0.5학점과 1.0학점 과목 동시 신청 시
 * - 0.5학점만 성공하고 1.0학점은 실패해야 함

 * 테스트 시나리오:
 * 1. 학생이 이미 17.5학점 신청한 상태로 준비
 * 2. 0.5학점 과목과 1.0학점 과목에 동시 신청
 * 3. 0.5학점은 성공 (총 18.0), 1.0학점은 실패 (18.5 초과)

 * 실행 전 준비:
 * 1. test_setup.sql에 0.5학점, 1.0학점 과목 추가 필요
 * 2. 학생이 미리 17.5학점 신청한 상태로 설정

 * 예상 결과:
 * - 0.5학점 과목: 성공 (18.0학점)
 * - 1.0학점 과목: 실패 (18.5학점 초과)
 *
 * @author Park
 * @since 2025-12-05
 */
public class CreditBoundaryTest {

    // 실제 ID로 교체 필요
    private static final String STUDENT_ID = "TEST00020";
    private static final int COURSE_HALF = 878; // 0.5학점 과목 (추가 필요)
    private static final int COURSE_ONE = 879;  // 1.0학점 과목 (추가 필요)

    private static final double MAX_CREDITS = 18.0;
    private static final double CURRENT_CREDITS = 17.5; // 이미 신청한 학점

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================================");
        System.out.println(" 학점 제한 경계값 테스트");
        System.out.println("=================================================================");
        System.out.println("목표: 최대 학점 경계에서 수강신청 제어 검증");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("학생 ID: " + STUDENT_ID);
        System.out.println("최대 학점: " + MAX_CREDITS);
        System.out.println("현재 신청: " + CURRENT_CREDITS + "학점");
        System.out.println("잔여 가능: " + (MAX_CREDITS - CURRENT_CREDITS) + "학점");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("시나리오:");
        System.out.println("  - 0.5학점 과목 신청 → 성공 예상 (총 18.0)");
        System.out.println("  - 1.0학점 과목 신청 → 실패 예상 (총 18.5 초과)");
        System.out.println("=================================================================\n");

        EnrollmentDAO dao = new EnrollmentDAO();
        OpenCourseDAO courseDAO = new OpenCourseDAO();

        // Step 0: 현재 신청 학점 확인
        System.out.println("Step 0: 현재 신청 학점 확인");
        double currentTotal = dao.getTotalCredits(STUDENT_ID, 2025, "2학기");
        System.out.println("  현재 총 학점: " + currentTotal);

        if (Math.abs(currentTotal - CURRENT_CREDITS) > 0.1) {
            System.err.println("\n경고: 예상 학점(" + CURRENT_CREDITS + ")과 다릅니다!");
            System.err.println("  테스트 전에 데이터를 " + CURRENT_CREDITS + "학점으로 맞춰주세요.");
        }

        System.out.println("\n-----------------------------------------------------------------");

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // =====================================================================
        // Thread 1: 0.5학점 과목 신청
        // =====================================================================
        Thread thread1 = new Thread(() -> {
            try {
                startLatch.await();

                System.out.println("\n[Thread 1] 0.5학점 과목 신청 시도...");
                boolean success = dao.applyCourse(STUDENT_ID, COURSE_HALF);

                if (success) {
                    successCount.incrementAndGet();
                    System.out.println("[Thread 1] 성공! (총 18.0학점)");
                } else {
                    failCount.incrementAndGet();
                    System.out.println("[Thread 1] 실패! (예상과 다름)");
                }

            } catch (Exception e) {
                failCount.incrementAndGet();
                System.err.println("[Thread 1] 에러: " + e.getMessage());
            } finally {
                doneLatch.countDown();
            }
        }, "Thread-0.5");

        // =====================================================================
        // Thread 2: 1.0학점 과목 신청
        // =====================================================================
        Thread thread2 = new Thread(() -> {
            try {
                startLatch.await();

                System.out.println("\n[Thread 2] 1.0학점 과목 신청 시도");
                boolean success = dao.applyCourse(STUDENT_ID, COURSE_ONE);

                if (success) {
                    successCount.incrementAndGet();
                    System.out.println("[Thread 2] 성공! (예상과 다름)");
                } else {
                    failCount.incrementAndGet();
                    System.out.println("[Thread 2] 실패! (학점 초과로 거부됨)");
                }

            } catch (Exception e) {
                failCount.incrementAndGet();
                System.err.println("[Thread 2] 에러: " + e.getMessage());
            } finally {
                doneLatch.countDown();
            }
        }, "Thread-1.0");

        // 스레드 시작
        thread1.start();
        thread2.start();

        System.out.println("\n3초 후 동시 신청");
        Thread.sleep(3000);

        System.out.println("\n 동시 신청 시작! \n");
        startLatch.countDown();

        doneLatch.await();
        Thread.sleep(1000);

        // 최종 학점 확인
        double finalTotal = dao.getTotalCredits(STUDENT_ID, 2025, "2학기");

        // 결과 출력
        System.out.println("\n=================================================================");
        System.out.println("   테스트 결과");
        System.out.println("=================================================================");
        System.out.println("시작 학점: " + CURRENT_CREDITS);
        System.out.println("최종 학점: " + finalTotal);
        System.out.println("성공 건수: " + successCount.get());
        System.out.println("실패 건수: " + failCount.get());
        System.out.println("-----------------------------------------------------------------");

        boolean isCorrect = (successCount.get() == 1 &&
                failCount.get() == 1 &&
                Math.abs(finalTotal - 18.0) < 0.1);

        if (isCorrect) {
            System.out.println("[PASS] 학점 제한이 올바르게 작동합니다!");
            System.out.println("   0.5학점 과목만 신청되어 정확히 18.0학점이 되었습니다.");
            System.out.println("\n 경계값 테스트의 중요성:");
            System.out.println("   - Off-by-One 에러 감지");
            System.out.println("   - 부동소수점 연산 오차 확인");
            System.out.println("   - 동시성 환경에서 일관성 보장");
        } else if (successCount.get() == 2) {
            System.out.println("[FAIL] 두 과목 모두 신청되었습니다!");
            System.out.println("   최종 학점: " + finalTotal + " (18.0 초과)");
            System.out.println("   → 학점 검증 로직이 작동하지 않습니다.");
        } else if (successCount.get() == 0) {
            System.out.println("[FAIL] 두 과목 모두 실패했습니다!");
            System.out.println("   → 데이터 상태를 확인하세요.");
        } else {
            System.out.println("부분 성공");
            System.out.println("   최종 학점: " + finalTotal);
            System.out.println("   → 예상과 다른 결과입니다.");
        }

        System.out.println("=================================================================\n");

        System.out.println("추가 테스트 아이디어:");
        System.out.println("   1. 17.9학점에서 0.05학점 과목 신청 (부동소수점 오차)");
        System.out.println("   2. 정확히 18.0학점 상태에서 0.01학점 과목 신청");
        System.out.println("   3. 우수자(21학점) 경계 테스트");
    }
}