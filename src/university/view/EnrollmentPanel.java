package university.view;

import university.dao.EnrollmentDAO;
import university.dao.OpenCourseDAO;
import university.model.Enrollment;
import university.model.OpenCourse;
import university.service.EnrollmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 수강신청 패널 (수정 완료: ClassCastException 해결 + UI 개선)
 */
public class EnrollmentPanel extends JPanel {

    private OpenCourseDAO openCourseDAO;
    private EnrollmentDAO enrollmentDAO;
    private EnrollmentService enrollmentService;

    // 개설강좌 테이블
    private JTable courseTable;
    private DefaultTableModel courseTableModel;
    private String[] courseColumns = {"ID", "과목명", "학점", "분반", "교수", "정원", "신청", "잔여"};

    // 수강신청 테이블
    private JTable enrollmentTable;
    private DefaultTableModel enrollmentTableModel;
    private String[] enrollmentColumns = {"신청ID", "과목명", "학점", "교수", "상태", "신청일시"};

    private JTextField txtStudentId;
    private JTextField txtSearchYear, txtSearchCourse;
    private JComboBox<String> cbSearchTerm;

    // 총 학점 라벨을 멤버 변수로 선언 (안전성 향상)
    private JLabel lblTotalCredits;

    public EnrollmentPanel() {
        this.openCourseDAO = new OpenCourseDAO();
        this.enrollmentDAO = new EnrollmentDAO();
        this.enrollmentService = new EnrollmentService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
    }

    private void initComponents() {
        // 상단 패널 (학생 정보 & 검색)
        add(createTopPanel(), BorderLayout.NORTH);

        // 중앙 분할 패널
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setTopComponent(createCourseListPanel());
        splitPane.setBottomComponent(createEnrollmentListPanel());

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * 상단 패널 (학생 ID 입력 & 개설강좌 검색)
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(52, 152, 219));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 학생 정보 입력 패널
        JPanel studentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentPanel.setBackground(new Color(52, 152, 219));

        JLabel lblStudentId = new JLabel("학번:");
        lblStudentId.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        lblStudentId.setForeground(Color.WHITE);

        txtStudentId = new JTextField(15);
        txtStudentId.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnLoadEnrollment = new JButton("내 수강신청 조회");
        btnLoadEnrollment.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnLoadEnrollment.setBackground(new Color(46, 204, 113));
        btnLoadEnrollment.setForeground(Color.WHITE);
        btnLoadEnrollment.setOpaque(true);
        btnLoadEnrollment.setBorderPainted(false);
        btnLoadEnrollment.addActionListener(e -> loadMyEnrollments());

        studentPanel.add(lblStudentId);
        studentPanel.add(txtStudentId);
        studentPanel.add(btnLoadEnrollment);

        // 개설강좌 검색 패널
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(new Color(52, 152, 219));

        JLabel lblSearch = new JLabel("개설강좌 검색:");
        lblSearch.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        lblSearch.setForeground(Color.WHITE);

        txtSearchYear = new JTextField("2025", 6);
        txtSearchYear.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        cbSearchTerm = new JComboBox<>(new String[]{"1학기", "2학기", "여름학기", "겨울학기"});
        cbSearchTerm.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        cbSearchTerm.setSelectedItem("2학기");

        JButton btnSearchCourse = new JButton("조회");
        btnSearchCourse.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnSearchCourse.setBackground(Color.WHITE);
        btnSearchCourse.setOpaque(true);
        btnSearchCourse.setBorderPainted(false);
        btnSearchCourse.addActionListener(e -> loadOpenCourses(true));

        searchPanel.add(lblSearch);
        searchPanel.add(new JLabel("학년도:"));
        searchPanel.add(txtSearchYear);
        searchPanel.add(new JLabel("학기:"));
        searchPanel.add(cbSearchTerm);
        searchPanel.add(btnSearchCourse);

        panel.add(studentPanel);
        panel.add(searchPanel);

        return panel;
    }

    /**
     * 개설강좌 목록 패널
     */
    private JPanel createCourseListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "개설강좌 목록",
                0, 0,
                new Font("맑은 고딕", Font.BOLD, 14)
        ));

        // 테이블
        courseTableModel = new DefaultTableModel(courseColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        courseTable = new JTable(courseTableModel);
        courseTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        courseTable.setRowHeight(25);
        courseTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(courseTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 수강신청 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEnroll = new JButton("수강신청");
        btnEnroll.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnEnroll.setBackground(new Color(46, 204, 113));
        btnEnroll.setForeground(Color.WHITE);
        btnEnroll.setOpaque(true);
        btnEnroll.setBorderPainted(false);
        btnEnroll.addActionListener(e -> enrollCourse());

        buttonPanel.add(btnEnroll);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 내 수강신청 목록 패널
     */
    private JPanel createEnrollmentListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "내 수강신청 목록",
                0, 0,
                new Font("맑은 고딕", Font.BOLD, 14)
        ));

        // 테이블
        enrollmentTableModel = new DefaultTableModel(enrollmentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        enrollmentTable = new JTable(enrollmentTableModel);
        enrollmentTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        enrollmentTable.setRowHeight(25);
        enrollmentTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(enrollmentTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 수강취소 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // 멤버 변수에 할당 (안전성 향상)
        lblTotalCredits = new JLabel("총 신청학점: 0.0 학점");
        lblTotalCredits.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        lblTotalCredits.setForeground(new Color(41, 128, 185));

        JButton btnCancel = new JButton("수강취소");
        btnCancel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnCancel.setBackground(new Color(231, 76, 60));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setOpaque(true);
        btnCancel.setBorderPainted(false);
        btnCancel.addActionListener(e -> cancelEnrollment());

        buttonPanel.add(lblTotalCredits);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnCancel);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 개설강좌 목록 조회
     */
    private void loadOpenCourses() {
        loadOpenCourses(false);
    }

    /**
     * 개설강좌 목록 조회 (팝업 표시 옵션)
     */
    private void loadOpenCourses(boolean showMessage) {
        try {
            int year = Integer.parseInt(txtSearchYear.getText().trim());
            String term = (String) cbSearchTerm.getSelectedItem();

            courseTableModel.setRowCount(0);
            List<OpenCourse> list = openCourseDAO.selectByYearAndTerm(year, term);

            for (OpenCourse oc : list) {
                Object[] row = {
                        oc.getOpenCourseId(),
                        oc.getCourseNameKr(),
                        oc.getCredit(),
                        oc.getSection() + "분반",
                        oc.getProfessorName(),
                        oc.getCapacity(),
                        oc.getEnrolledCount(),
                        oc.getRemainingCapacity()
                };
                courseTableModel.addRow(row);
            }

            if (showMessage) {
                JOptionPane.showMessageDialog(this,
                        "개설강좌 " + list.size() + "개를 조회했습니다.",
                        "조회 완료", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("개설강좌 " + list.size() + "개 조회 완료");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "학년도를 올바르게 입력하세요.",
                    "입력 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 내 수강신청 목록 조회
     */
    private void loadMyEnrollments() {
        String studentId = txtStudentId.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "학번을 입력하세요.",
                    "입력 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int year = Integer.parseInt(txtSearchYear.getText().trim());
            String term = (String) cbSearchTerm.getSelectedItem();

            enrollmentTableModel.setRowCount(0);
            List<Enrollment> list = enrollmentDAO.selectByStudent(studentId, year, term);

            double totalCredits = 0.0;
            for (Enrollment enrollment : list) {
                Object[] row = {
                        enrollment.getEnrollmentId(),
                        enrollment.getCourseNameKr(),
                        enrollment.getCredit(),
                        enrollment.getProfessorName(),
                        enrollment.getStatusKorean(),
                        enrollment.getRequestedAt()
                };
                enrollmentTableModel.addRow(row);
                totalCredits += enrollment.getCredit();
            }

            // 안전하게 멤버 변수로 텍스트 업데이트
            if (lblTotalCredits != null) {
                lblTotalCredits.setText(String.format("총 신청학점: %.1f 학점", totalCredits));
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "학년도를 올바르게 입력하세요.",
                    "입력 오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 수강신청
     */
    private void enrollCourse() {
        String studentId = txtStudentId.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "학번을 먼저 입력하세요.",
                    "입력 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "수강신청할 강좌를 선택하세요.",
                    "선택 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int openCourseId = Integer.parseInt(courseTableModel.getValueAt(selectedRow, 0).toString());
        String courseName = courseTableModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "'" + courseName + "' 강좌를 수강신청하시겠습니까?",
                "수강신청 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            EnrollmentService.EnrollmentResult result =
                    enrollmentService.enroll(studentId, openCourseId, studentId);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        result.getMessage(),
                        "성공", JOptionPane.INFORMATION_MESSAGE);
                loadOpenCourses(false);
                loadMyEnrollments();
            } else {
                JOptionPane.showMessageDialog(this,
                        result.getMessage(),
                        "수강신청 실패", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 수강취소
     */
    private void cancelEnrollment() {
        int selectedRow = enrollmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "취소할 수강신청을 선택하세요.",
                    "선택 필요", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int enrollmentId = Integer.parseInt(enrollmentTableModel.getValueAt(selectedRow, 0).toString());
        String courseName = enrollmentTableModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "'" + courseName + "' 수강신청을 취소하시겠습니까?",
                "수강취소 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            EnrollmentService.EnrollmentResult result =
                    enrollmentService.cancelEnrollment(enrollmentId);

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        result.getMessage(),
                        "성공", JOptionPane.INFORMATION_MESSAGE);
                loadOpenCourses(false);
                loadMyEnrollments();
            } else {
                JOptionPane.showMessageDialog(this,
                        result.getMessage(),
                        "취소 실패", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}