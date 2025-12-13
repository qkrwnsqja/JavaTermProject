package university.scenario;

import university.dao.GradeDAO;

public class GradeConflictTest {

    public static void main(String[] args) {
        System.out.println("=== [시나리오 8] 성적 입력 동시성 테스트 (Lost Update 방지) ===");

        GradeDAO dao = new GradeDAO();

        // 1. 테스트할 성적 데이터 ID 가져오기
        int targetGradeId = dao.getLastGradeId();

        if (targetGradeId == 0) {
            System.out.println("테스트할 성적 데이터가 없습니다. SQL을 먼저 실행해서 데이터를 준비해주세요.");
            return;
        }

        System.out.println("대상 성적 ID: " + targetGradeId);
        System.out.println("---------------------------------------------");

        // -------------------------------------------------------
        // Thread 1: PROF001 (먼저 들어와서 3초 동안 락을 쥐고 있음)
        // -------------------------------------------------------
        Thread threadA = new Thread(() -> {
            // "김교수(A)" -> 실제 ID "PROF001"로 변경
            dao.updateFinalScoreWithLock(targetGradeId, 90.0, "PROF001", 3000);
        });

        // -------------------------------------------------------
        // Thread 2: PROF002 (0.5초 뒤에 진입 시도 -> 대기해야 함)
        // -------------------------------------------------------
        Thread threadB = new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) {}

            System.out.println(">>> [이교수(PROF002)] 진입 시도! (김교수가 락을 쥐고 있어서 대기해야 정상)");

            // "이교수(B)" -> 실제 ID "PROF002"로 변경
            dao.updateFinalScoreWithLock(targetGradeId, 80.0, "PROF002", 0);
        });

        // 스레드 시작
        threadA.start();
        threadB.start();

        // 스레드 종료 대기
        try {
            threadA.join();
            threadB.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("---------------------------------------------");
        System.out.println("   테스트 종료.");
        System.out.println("   [성공 기준]");
        System.out.println("   1. [PROF001]이 커밋했다는 로그가 뜬 후에야 [PROF002]가 락을 획득해야 함.");
        System.out.println("   2. DB 조회 시 최종 점수는 나중에 실행된 '80점'이어야 함.");
    }
}