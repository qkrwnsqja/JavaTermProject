package university.view;

import javax.swing.*;
import java.awt.*;

/**
 * 대학 학사관리 시스템 메인 프레임
 */
public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    // 각 패널
    private DepartmentPanel departmentPanel;
    private StudentManagementPanel studentPanel;
    private ProfessorPanel professorPanel;
    private CourseManagementPanel coursePanel;
    private OpenCoursePanel openCoursePanel;
    private EnrollmentPanel enrollmentPanel;
    private GradePanel gradePanel;

    public MainFrame() {
        setTitle("대학 학사관리 시스템");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();

        setVisible(true);
    }

    private void initComponents() {
        // 메인 레이아웃
        setLayout(new BorderLayout());

        // 상단 패널 (제목)
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // 중앙 탭 패널
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        // 각 탭 추가
        departmentPanel = new DepartmentPanel();
        tabbedPane.addTab("학과 관리", departmentPanel);

        studentPanel = new StudentManagementPanel();
        tabbedPane.addTab("학생 관리", studentPanel);

        professorPanel = new ProfessorPanel();
        tabbedPane.addTab("교수 관리", professorPanel);

        coursePanel = new CourseManagementPanel();
        tabbedPane.addTab("과목 관리", coursePanel);

        openCoursePanel = new OpenCoursePanel();
        tabbedPane.addTab("개설강좌 관리", openCoursePanel);

        enrollmentPanel = new EnrollmentPanel();
        tabbedPane.addTab("수강신청", enrollmentPanel);

        gradePanel = new GradePanel();
        tabbedPane.addTab("성적 관리", gradePanel);

        // 탭 변경 리스너 추가 (데이터 새로고침)
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch (selectedIndex) {
                case 1: // 학생 관리
                    studentPanel.refreshData();
                    break;
                case 2: // 교수 관리
                    professorPanel.refreshData();
                    break;
                case 4: // 개설강좌 관리
                    openCoursePanel.refreshData();
                    break;
            }
        });

        add(tabbedPane, BorderLayout.CENTER);

        // 하단 상태바
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }

    /**
     * 헤더 패널 생성
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(41, 128, 185));
        panel.setPreferredSize(new Dimension(0, 80));
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("대학 학사관리 시스템", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        panel.add(titleLabel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 상태바 패널 생성
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.setBackground(new Color(236, 240, 241));

        JLabel statusLabel = new JLabel("시스템 정상 작동 중");
        statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        JLabel dateLabel = new JLabel("2025년 2학기");
        dateLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(dateLabel, BorderLayout.EAST);

        return panel;
    }

    public static void main(String[] args) {
        // Look and Feel 설정
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // GUI는 EDT(Event Dispatch Thread)에서 실행
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}