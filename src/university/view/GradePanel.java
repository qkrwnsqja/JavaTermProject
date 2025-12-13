package university.view;

import university.dao.EnrollmentDAO;
import university.dao.GradeDAO;
import university.model.Enrollment;
import university.model.Grade;
import university.service.GradeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 성적 관리 패널
 */
public class GradePanel extends JPanel {

    private EnrollmentDAO enrollmentDAO;
    private GradeDAO gradeDAO;
    private GradeService gradeService;

    private JTable enrollmentTable;
    private DefaultTableModel enrollmentTableModel;
    private String[] enrollmentColumns = {"수강ID", "학생", "과목명", "학점", "교수"};

    private JTable gradeTable;
    private DefaultTableModel gradeTableModel;
    private String[] gradeColumns = {"과목명", "학점", "중간", "기말", "평균", "성적", "평점", "확정"};

    private JTextField txtSearchYear, txtSearchStudent;
    private JComboBox<String> cbSearchTerm;
    private JTextField txtMidterm, txtFinal;
    private JLabel lblCalculatedGrade, lblGPA;

    private int selectedEnrollmentId = -1;

    public GradePanel() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.gradeDAO = new GradeDAO();
        this.gradeService = new GradeService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
    }

    private void initComponents() {
        // 좌측 패널 (교수용 - 성적 입력)
        JPanel leftPanel = createProfessorPanel();

        // 우측 패널 (학생용 - 성적 조회)
        JPanel rightPanel = createStudentPanel();

        // 분할 패널
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * 교수용 패널 - 성적 입력
     */
    private JPanel createProfessorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                "교수용 - 성적 입력",
                0, 0,
                new Font("맑은 고딕", Font.BOLD, 16)
        ));

        // 상단 검색
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("강좌 검색:");
        lblSearch.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        txtSearchYear = new JTextField("2025", 6);
        cbSearchTerm = new JComboBox<>(new String[]{"1학기", "2학기", "여름학기", "겨울학기"});
        cbSearchTerm.setSelectedItem("2학기");

        JTextField txtOpenCourseId = new JTextField(8);

        JButton btnSearch = new JButton("수강생 조회");
        btnSearch.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnSearch.setOpaque(true);
        btnSearch.setBorderPainted(false);
        btnSearch.addActionListener(e -> {
            try {
                int openCourseId = Integer.parseInt(txtOpenCourseId.getText().trim());
                loadEnrollmentsByOpenCourse(openCourseId);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "개설강좌 ID를 입력하세요.");
            }
        });

        searchPanel.add(lblSearch);
        searchPanel.add(new JLabel("개설강좌ID:"));
        searchPanel.add(txtOpenCourseId);
        searchPanel.add(btnSearch);

        panel.add(searchPanel, BorderLayout.NORTH);

        // 중앙 테이블
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

        enrollmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = enrollmentTable.getSelectedRow();
                if (row != -1) {
                    try {
                        Object idValue = enrollmentTableModel.getValueAt(row, 0);
                        if (idValue != null) {
                            selectedEnrollmentId = Integer.parseInt(idValue.toString());
                            System.out.println("선택된 수강ID: " + selectedEnrollmentId);
                            loadGradeForEnrollment(selectedEnrollmentId);
                        }
                    } catch (Exception ex) {
                        System.err.println("수강ID 파싱 오류: " + ex.getMessage());
                    }
                }
            }
        });

        panel.add(new JScrollPane(enrollmentTable), BorderLayout.CENTER);

        // 하단 성적 입력 폼
        panel.add(createGradeInputPanel(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 성적 입력 폼 패널
     */
    private JPanel createGradeInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("성적 입력");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 점수 입력
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scorePanel.setBackground(new Color(236, 240, 241));
        scorePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        scorePanel.add(new JLabel("중간고사:"));
        txtMidterm = new JTextField(8);
        scorePanel.add(txtMidterm);

        scorePanel.add(Box.createHorizontalStrut(20));
        scorePanel.add(new JLabel("기말고사:"));
        txtFinal = new JTextField(8);
        scorePanel.add(txtFinal);

        JButton btnCalculate = new JButton("성적 계산");
        btnCalculate.setFont(new Font("맑은 고딕", Font.BOLD, 11));
        btnCalculate.setOpaque(true);
        btnCalculate.setBorderPainted(false);
        btnCalculate.addActionListener(e -> calculateGrade());
        scorePanel.add(btnCalculate);

        panel.add(scorePanel);

        // 계산된 성적 표시
        lblCalculatedGrade = new JLabel("계산된 성적: -");
        lblCalculatedGrade.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        lblCalculatedGrade.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(lblCalculatedGrade);

        // 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(new Color(236, 240, 241));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave = new JButton("저장");
        btnSave.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        btnSave.addActionListener(e -> saveGrade());

        JButton btnClear = new JButton("초기화");
        btnClear.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnClear.setOpaque(true);
        btnClear.setBorderPainted(false);
        btnClear.addActionListener(e -> clearGradeForm());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnClear);
        panel.add(buttonPanel);

        return panel;
    }

    /**
     * 학생용 패널 - 성적 조회
     */
    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113), 2),
                "학생용 - 성적 조회",
                0, 0,
                new Font("맑은 고딕", Font.BOLD, 16)
        ));

        // 상단 검색
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("학생 검색:");
        lblSearch.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        txtSearchStudent = new JTextField(12);

        JTextField txtYear = new JTextField("2025", 6);
        JComboBox<String> cbTerm = new JComboBox<>(new String[]{"1학기", "2학기", "여름학기", "겨울학기"});
        cbTerm.setSelectedItem("2학기");

        JButton btnSearch = new JButton("조회");
        btnSearch.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnSearch.setOpaque(true);
        btnSearch.setBorderPainted(false);
        btnSearch.addActionListener(e -> {
            String studentId = txtSearchStudent.getText().trim();
            if (!studentId.isEmpty()) {
                try {
                    int year = Integer.parseInt(txtYear.getText().trim());
                    String term = (String) cbTerm.getSelectedItem();
                    loadStudentGrades(studentId, year, term);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "학년도를 올바르게 입력하세요.");
                }
            }
        });

        searchPanel.add(lblSearch);
        searchPanel.add(new JLabel("학번:"));
        searchPanel.add(txtSearchStudent);
        searchPanel.add(new JLabel("학년도:"));
        searchPanel.add(txtYear);
        searchPanel.add(new JLabel("학기:"));
        searchPanel.add(cbTerm);
        searchPanel.add(btnSearch);

        panel.add(searchPanel, BorderLayout.NORTH);

        // 성적 테이블
        gradeTableModel = new DefaultTableModel(gradeColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gradeTable = new JTable(gradeTableModel);
        gradeTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        gradeTable.setRowHeight(25);
        gradeTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));

        panel.add(new JScrollPane(gradeTable), BorderLayout.CENTER);

        // 하단 평점 표시
        JPanel gpaPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        gpaPanel.setBackground(Color.WHITE);

        lblGPA = new JLabel("평균 평점: 0.00 / 4.50");
        lblGPA.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        lblGPA.setForeground(new Color(41, 128, 185));

        gpaPanel.add(lblGPA);
        panel.add(gpaPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 개설강좌별 수강생 조회
     */
    private void loadEnrollmentsByOpenCourse(int openCourseId) {
        enrollmentTableModel.setRowCount(0);
        List<Enrollment> list = enrollmentDAO.selectByOpenCourse(openCourseId);

        for (Enrollment enrollment : list) {
            Object[] row = {
                    enrollment.getEnrollmentId(),
                    enrollment.getStudentName(),
                    enrollment.getCourseNameKr(),
                    enrollment.getCredit(),
                    enrollment.getProfessorName()
            };
            enrollmentTableModel.addRow(row);
        }

        JOptionPane.showMessageDialog(this,
                "수강생 " + list.size() + "명을 조회했습니다.");
    }

    /**
     * 수강신청에 대한 성적 조회
     */
    private void loadGradeForEnrollment(int enrollmentId) {
        Grade grade = gradeDAO.selectByEnrollmentId(enrollmentId);

        if (grade != null) {
            // 기존 성적이 있으면 불러오기
            txtMidterm.setText(grade.getMidtermScore() != null ?
                    grade.getMidtermScore().toString() : "");
            txtFinal.setText(grade.getFinalScore() != null ?
                    grade.getFinalScore().toString() : "");

            if (grade.getFinalGrade() != null && grade.getGradePoint() != null) {
                lblCalculatedGrade.setText(String.format("계산된 성적: %s (%.1f점)",
                        grade.getFinalGrade(), grade.getGradePoint()));
            } else {
                lblCalculatedGrade.setText("계산된 성적: -");
            }
        } else {
            // 성적이 없으면 초기화
            txtMidterm.setText("");
            txtFinal.setText("");
            lblCalculatedGrade.setText("계산된 성적: -");
        }

        // selectedEnrollmentId 저장 (선택 유지)
        selectedEnrollmentId = enrollmentId;
    }

    /**
     * 성적 계산
     */
    private void calculateGrade() {
        try {
            Double midterm = txtMidterm.getText().trim().isEmpty() ? null :
                    Double.parseDouble(txtMidterm.getText().trim());
            Double finalScore = txtFinal.getText().trim().isEmpty() ? null :
                    Double.parseDouble(txtFinal.getText().trim());

            GradeService.GradeCalculation calc = gradeService.calculateGrade(midterm, finalScore);

            if (calc.getLetterGrade() != null) {
                lblCalculatedGrade.setText(String.format("계산된 성적: %s (%.1f점) - %s",
                        calc.getLetterGrade(), calc.getGradePoint(), calc.getMessage()));
            } else {
                lblCalculatedGrade.setText("계산된 성적: " + calc.getMessage());
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "점수는 숫자로 입력하세요.");
        }
    }

    /**
     * 성적 저장
     */
    private void saveGrade() {
        // 테이블에서 선택된 행 확인
        int selectedRow = enrollmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "학생을 선택하세요.");
            return;
        }

        // 선택된 행에서 enrollment_id 가져오기
        try {
            Object idValue = enrollmentTableModel.getValueAt(selectedRow, 0);
            if (idValue == null) {
                JOptionPane.showMessageDialog(this, "수강신청 정보를 불러올 수 없습니다.");
                return;
            }
            int enrollmentId = Integer.parseInt(idValue.toString());

            Double midterm = txtMidterm.getText().trim().isEmpty() ? null :
                    Double.parseDouble(txtMidterm.getText().trim());
            Double finalScore = txtFinal.getText().trim().isEmpty() ? null :
                    Double.parseDouble(txtFinal.getText().trim());

            GradeService.GradeResult result =
                    gradeService.inputGrade(enrollmentId, midterm, finalScore, "PROF001");

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(this, result.getMessage());
                clearGradeForm();
            } else {
                JOptionPane.showMessageDialog(this, result.getMessage(),
                        "오류", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "점수는 숫자로 입력하세요.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "오류가 발생했습니다: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 학생 성적 조회
     */
    private void loadStudentGrades(String studentId, int year, String term) {
        gradeTableModel.setRowCount(0);
        List<Grade> list = gradeDAO.selectByStudent(studentId, year, term);

        for (Grade grade : list) {
            Double avg = grade.getAverageScore();
            Object[] row = {
                    grade.getCourseNameKr(),
                    grade.getCredit(),
                    grade.getMidtermScore() != null ? grade.getMidtermScore() : "-",
                    grade.getFinalScore() != null ? grade.getFinalScore() : "-",
                    avg != null ? String.format("%.1f", avg) : "-",
                    grade.getFinalGrade() != null ? grade.getFinalGrade() : "-",
                    grade.getGradePoint() != null ? grade.getGradePoint() : "-",
                    grade.isConfirmed() ? "확정" : "미확정"
            };
            gradeTableModel.addRow(row);
        }

        // 평점 계산
        double gpa = gradeService.calculateStudentGPA(studentId);
        lblGPA.setText(String.format("평균 평점: %.2f / 4.50", gpa));
    }

    /**
     * 성적 입력 폼 초기화
     */
    private void clearGradeForm() {
        txtMidterm.setText("");
        txtFinal.setText("");
        lblCalculatedGrade.setText("계산된 성적: -");
        selectedEnrollmentId = -1;
    }
}