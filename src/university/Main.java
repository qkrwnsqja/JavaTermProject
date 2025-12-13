package university;

import university.view.MainFrame;

import javax.swing.*;

/**
 * 대학 학사관리 시스템 메인 실행 클래스
 *
 * @author Park
 * @version 1.0
 * @since 2025-12-03
 */
public class Main {

    public static void main(String[] args) {
        // Look and Feel 설정 (시스템 기본 스타일 사용)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Look and Feel 설정 실패: " + e.getMessage());
        }

        // GUI는 EDT(Event Dispatch Thread)에서 실행
        SwingUtilities.invokeLater(() -> {
            System.out.println("========================================");
            System.out.println("   대학 학사관리 시스템 시작");
            System.out.println("   Java JDBC Programming Project");
            System.out.println("   2025년 2학기 기말고사 대체 과제");
            System.out.println("========================================");

            // 메인 프레임 실행
            new MainFrame();
        });
    }
}