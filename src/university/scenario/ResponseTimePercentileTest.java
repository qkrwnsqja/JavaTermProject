package university.scenario;

import university.dao.EnrollmentDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 응답 시간 백분위수 측정 테스트

 * 목적:
 * - 대규모 동시 접속 시 응답 시간 분포 측정
 * - P50 (중앙값), P95, P99 백분위수 계산
 * - 성능 SLA 검증

 * 성능 목표 (예시):
 * - P50: 100ms 이하
 * - P95: 500ms 이하
 * - P99: 1000ms 이하

 * 백분위수란?
 * - P50 (중앙값): 50%의 요청이 이 시간 이내에 완료
 * - P95: 95%의 요청이 이 시간 이내에 완료
 * - P99: 99%의 요청이 이 시간 이내에 완료

 * 실행 전 준비:
 * 1. test_setup.sql 실행
 * 2. COURSE_ID 설정 (정원 넉넉한 강좌)

 * 예상 결과:
 * - 응답 시간 히스토그램 출력
 * - P50, P95, P99 값 계산
 * - SLA 충족 여부 판단
 *
 * @author Park
 * @since 2025-12-05
 */
public class ResponseTimePercentileTest {

    private static final int THREAD_COUNT = 100; // 500명 동시 접속
    private static final int COURSE_ID = 881;   // TEST_LEAK (정원 100명)

    // SLA 목표값 (밀리초)
    private static final long TARGET_P50 = 200;   // 200ms
    private static final long TARGET_P95 = 800;   // 800ms
    private static final long TARGET_P99 = 2000;  // 2000ms

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);
    private static final ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=================================================================");
        System.out.println(" 응답 시간 백분위수 측정 테스트");
        System.out.println("=================================================================");
        System.out.println("목표: 대규모 동시 접속 시 성능 SLA 검증");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("동시 접속자: " + THREAD_COUNT + "명");
        System.out.println("대상 강좌: " + COURSE_ID + " (정원 100명)");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("성능 목표:");
        System.out.println("  P50 (중앙값): " + TARGET_P50 + "ms 이하");
        System.out.println("  P95: " + TARGET_P95 + "ms 이하");
        System.out.println("  P99: " + TARGET_P99 + "ms 이하");
        System.out.println("=================================================================\n");

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);

        EnrollmentDAO dao = new EnrollmentDAO();

        // 500개 스레드 생성
        for (int i = 1; i <= THREAD_COUNT; i++) {
            final String studentId = "TEST" + String.format("%05d", i + 100);

            executor.submit(() -> {
                try {
                    startLatch.await();

                    // 응답 시간 측정 시작
                    long startTime = System.nanoTime();

                    boolean success = dao.applyCourse(studentId, COURSE_ID);

                    long endTime = System.nanoTime();
                    long responseTimeMs = (endTime - startTime) / 1_000_000; // 나노초 → 밀리초

                    // 응답 시간 기록
                    responseTimes.add(responseTimeMs);

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

        System.out.println("500명 준비 완료. 3초 후 동시 접속\n");
        Thread.sleep(3000);

        System.out.println(" 테스트 시작! \n");
        long testStartTime = System.currentTimeMillis();
        startLatch.countDown();

        // 진행 상황 표시
        new Thread(() -> {
            try {
                while (!doneLatch.await(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    int completed = THREAD_COUNT - (int)doneLatch.getCount();
                    int percent = (completed * 100) / THREAD_COUNT;
                    System.out.println("진행 중 " + completed + "/" + THREAD_COUNT +
                            " (" + percent + "%)");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        doneLatch.await();
        long testEndTime = System.currentTimeMillis();
        executor.shutdown();

        System.out.println("\n테스트 완료! 결과 분석 중\n");

        // =====================================================================
        // 응답 시간 분석
        // =====================================================================

        List<Long> timeList = new ArrayList<>(responseTimes);
        Collections.sort(timeList);

        int totalRequests = timeList.size();
        long totalDuration = testEndTime - testStartTime;

        // 백분위수 계산
        long p50 = getPercentile(timeList, 50);
        long p75 = getPercentile(timeList, 75);
        long p90 = getPercentile(timeList, 90);
        long p95 = getPercentile(timeList, 95);
        long p99 = getPercentile(timeList, 99);

        // 통계 계산
        long min = timeList.get(0);
        long max = timeList.get(totalRequests - 1);
        double avg = timeList.stream().mapToLong(Long::longValue).average().orElse(0);

        // 결과 출력
        System.out.println("=================================================================");
        System.out.println("   테스트 결과");
        System.out.println("=================================================================");
        System.out.println("[기본 통계]");
        System.out.println("  총 요청 수: " + totalRequests);
        System.out.println("  성공: " + successCount.get() + "건");
        System.out.println("  실패: " + failCount.get() + "건");
        System.out.println("  총 소요 시간: " + totalDuration + "ms");
        System.out.println("  처리량: " + String.format("%.2f", (totalRequests * 1000.0) / totalDuration) + " req/s");
        System.out.println();
        System.out.println("[응답 시간 통계]");
        System.out.println("  최소 (Min): " + min + "ms");
        System.out.println("  평균 (Avg): " + String.format("%.2f", avg) + "ms");
        System.out.println("  최대 (Max): " + max + "ms");
        System.out.println();
        System.out.println("[백분위수 (Percentiles)]");
        printPercentile("P50 (중앙값)", p50, TARGET_P50);
        printPercentile("P75", p75, -1);
        printPercentile("P90", p90, -1);
        printPercentile("P95", p95, TARGET_P95);
        printPercentile("P99", p99, TARGET_P99);
        System.out.println();

        // 응답 시간 분포 히스토그램
        System.out.println("[응답 시간 분포]");
        printHistogram(timeList);

        // SLA 검증
        System.out.println("\n-----------------------------------------------------------------");
        System.out.println("[SLA 검증]");

        boolean p50Pass = p50 <= TARGET_P50;
        boolean p95Pass = p95 <= TARGET_P95;
        boolean p99Pass = p99 <= TARGET_P99;

        System.out.println("  P50: " + (p50Pass ? "PASS" : "FAIL") +
                " (" + p50 + "ms / 목표: " + TARGET_P50 + "ms)");
        System.out.println("  P95: " + (p95Pass ? "PASS" : "FAIL") +
                " (" + p95 + "ms / 목표: " + TARGET_P95 + "ms)");
        System.out.println("  P99: " + (p99Pass ? "PASS" : "FAIL") +
                " (" + p99 + "ms / 목표: " + TARGET_P99 + "ms)");

        System.out.println("-----------------------------------------------------------------");

        if (p50Pass && p95Pass && p99Pass) {
            System.out.println("[PASS] 모든 SLA를 충족합니다!");
            System.out.println("   시스템이 목표 성능을 달성했습니다.");
        } else {
            System.out.println("[경고] 일부 SLA를 충족하지 못했습니다.");
            System.out.println("   성능 최적화가 필요합니다.");
            System.out.println("\n개선 방법:");
            if (!p50Pass) {
                System.out.println("   - P50 개선: 인덱스 최적화, 쿼리 튜닝");
            }
            if (!p95Pass) {
                System.out.println("   - P95 개선: Connection Pool 크기 증가");
            }
            if (!p99Pass) {
                System.out.println("   - P99 개선: Lock Timeout 설정, 타임아웃 조정");
            }
        }

        System.out.println("=================================================================\n");

        // 추가 분석
        System.out.println("백분위수를 사용하는 이유:");
        System.out.println("   - 평균은 이상치(outlier)에 영향을 많이 받음");
        System.out.println("   - P95, P99는 최악의 사용자 경험을 대표");
        System.out.println("   - 실무에서는 평균보다 백분위수로 SLA 정의");
        System.out.println("\n예시: 평균 100ms, P99 5000ms");
        System.out.println("   → 99%는 빠르지만 1%는 매우 느림 (사용자 불만)");
    }

    /**
     * 백분위수 계산
     */
    private static long getPercentile(List<Long> sortedList, int percentile) {
        int index = (int) Math.ceil(sortedList.size() * percentile / 100.0) - 1;
        index = Math.max(0, Math.min(index, sortedList.size() - 1));
        return sortedList.get(index);
    }

    /**
     * 백분위수 출력 (목표값과 비교)
     */
    private static void printPercentile(String label, long value, long target) {
        String result = String.format("  %-15s: %5dms", label, value);
        if (target > 0) {
            boolean pass = value <= target;
            result += String.format("  %s (목표: %dms)", pass ? "✅" : "❌", target);
        }
        System.out.println(result);
    }

    /**
     * 히스토그램 출력
     */
    private static void printHistogram(List<Long> timeList) {
        long[] buckets = {0, 50, 100, 200, 500, 1000, 2000, 5000, Long.MAX_VALUE};
        String[] labels = {"0-50ms", "50-100ms", "100-200ms", "200-500ms",
                "500-1000ms", "1000-2000ms", "2000-5000ms", "5000ms+"};

        int[] counts = new int[buckets.length - 1];

        for (long time : timeList) {
            for (int i = 0; i < buckets.length - 1; i++) {
                if (time >= buckets[i] && time < buckets[i + 1]) {
                    counts[i]++;
                    break;
                }
            }
        }

        int maxCount = 0;
        for (int count : counts) {
            maxCount = Math.max(maxCount, count);
        }

        for (int i = 0; i < labels.length; i++) {
            int barLength = (counts[i] * 50) / Math.max(maxCount, 1);
            String bar = "█".repeat(barLength);
            double percent = (counts[i] * 100.0) / timeList.size();
            System.out.printf("  %-15s: %4d (%5.1f%%) %s%n",
                    labels[i], counts[i], percent, bar);
        }
    }
}