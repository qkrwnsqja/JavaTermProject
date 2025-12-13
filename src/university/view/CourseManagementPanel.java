package university.view;

import university.dao.CourseDAO;
import university.model.Course;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 과목 관리 패널
 */
public class CourseManagementPanel extends JPanel {

    private CourseDAO courseDAO;

    private JTable table;
    private DefaultTableModel tableModel;
    private String[] columnNames = {"과목코드", "과목명", "학점", "구분", "권장학년", "폐지여부"};

    private JTextField txtCourseCode, txtCourseNameKr, txtCourseNameEn;
    private JTextField txtCredit, txtRecommendedYear;
    private JComboBox<String> cbCourseType;
    private JTextField txtSearch;

    public CourseManagementPanel() {
        this.courseDAO = new CourseDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        loadData();
    }

    private void initComponents() {
        add(createSearchPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.EAST);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("과목 검색:");
        lblSearch.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnSearch = new JButton("검색");
        btnSearch.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnSearch.addActionListener(e -> searchCourse());

        JButton btnRefresh = new JButton("전체 조회");
        btnRefresh.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> loadData());

        panel.add(lblSearch);
        panel.add(txtSearch);
        panel.add(btnSearch);
        panel.add(btnRefresh);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    String courseCode = tableModel.getValueAt(row, 0).toString();
                    loadCourse(courseCode);
                }
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(236, 240, 241));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("과목 정보 입력");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        txtCourseCode = addFormField(panel, "과목 코드:");
        txtCourseNameKr = addFormField(panel, "한글 과목명:");
        txtCourseNameEn = addFormField(panel, "영문 과목명:");
        txtCredit = addFormField(panel, "학점:");

        // 과목 구분 콤보박스
        JLabel lblType = new JLabel("과목 구분:");
        lblType.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        lblType.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbCourseType = new JComboBox<>(new String[]{"전공필수", "전공선택", "교양필수", "교양선택"});
        cbCourseType.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        cbCourseType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbCourseType.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblType);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(cbCourseType);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        txtRecommendedYear = addFormField(panel, "권장 학년:");

        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(createButtonPanel());

        return panel;
    }

    private JTextField addFormField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField textField = new JTextField();
        textField.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(textField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        return textField;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnAdd = createStyledButton("등록", new Color(46, 204, 113));
        JButton btnUpdate = createStyledButton("수정", new Color(52, 152, 219));
        JButton btnDelete = createStyledButton("폐지", new Color(231, 76, 60));
        JButton btnClear = createStyledButton("초기화", new Color(149, 165, 166));

        btnAdd.addActionListener(e -> addCourse());
        btnUpdate.addActionListener(e -> updateCourse());
        btnDelete.addActionListener(e -> deleteCourse());
        btnClear.addActionListener(e -> clearForm());

        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnClear);

        return panel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        return button;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Course> list = courseDAO.selectAllIncludingDeleted();

        for (Course course : list) {
            Object[] row = {
                    course.getCourseCode(),
                    course.getCourseNameKr(),
                    course.getCredit(),
                    course.getCourseType(),
                    course.getRecommendedYear() != null ? course.getRecommendedYear() : "",
                    course.isDeleted() ? "폐지" : "정상"
            };
            tableModel.addRow(row);
        }
    }

    private void searchCourse() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        tableModel.setRowCount(0);
        List<Course> list = courseDAO.searchByName(keyword);

        for (Course course : list) {
            Object[] row = {
                    course.getCourseCode(),
                    course.getCourseNameKr(),
                    course.getCredit(),
                    course.getCourseType(),
                    course.getRecommendedYear() != null ? course.getRecommendedYear() : "",
                    course.isDeleted() ? "폐지" : "정상"
            };
            tableModel.addRow(row);
        }
    }

    private void loadCourse(String courseCode) {
        Course course = courseDAO.selectByCode(courseCode);
        if (course != null) {
            txtCourseCode.setText(course.getCourseCode());
            txtCourseNameKr.setText(course.getCourseNameKr());
            txtCourseNameEn.setText(course.getCourseNameEn());
            txtCredit.setText(String.valueOf(course.getCredit()));
            cbCourseType.setSelectedItem(course.getCourseType());
            txtRecommendedYear.setText(course.getRecommendedYear() != null ?
                    course.getRecommendedYear().toString() : "");
        }
    }

    private void addCourse() {
        if (!validateInput()) return;

        Course course = new Course();
        course.setCourseCode(txtCourseCode.getText().trim());
        course.setCourseNameKr(txtCourseNameKr.getText().trim());
        course.setCourseNameEn(txtCourseNameEn.getText().trim());
        course.setCredit(Double.parseDouble(txtCredit.getText().trim()));
        course.setCourseType((String)cbCourseType.getSelectedItem());

        String yearStr = txtRecommendedYear.getText().trim();
        if (!yearStr.isEmpty()) {
            course.setRecommendedYear(Integer.parseInt(yearStr));
        }
        course.setIsDeleted("N");

        if (courseDAO.insert(course)) {
            JOptionPane.showMessageDialog(this, "과목이 등록되었습니다.");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "등록에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCourse() {
        if (!validateInput()) return;

        Course course = new Course();
        course.setCourseCode(txtCourseCode.getText().trim());
        course.setCourseNameKr(txtCourseNameKr.getText().trim());
        course.setCourseNameEn(txtCourseNameEn.getText().trim());
        course.setCredit(Double.parseDouble(txtCredit.getText().trim()));
        course.setCourseType((String)cbCourseType.getSelectedItem());

        String yearStr = txtRecommendedYear.getText().trim();
        if (!yearStr.isEmpty()) {
            course.setRecommendedYear(Integer.parseInt(yearStr));
        }
        course.setIsDeleted("N");

        if (courseDAO.update(course)) {
            JOptionPane.showMessageDialog(this, "과목 정보가 수정되었습니다.");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "수정에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCourse() {
        String courseCode = txtCourseCode.getText().trim();
        if (courseCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "폐지할 과목을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "정말 폐지하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (courseDAO.delete(courseCode)) {
                JOptionPane.showMessageDialog(this, "과목이 폐지되었습니다.");
                clearForm();
                loadData();
            }
        }
    }

    private boolean validateInput() {
        if (txtCourseCode.getText().trim().isEmpty() || txtCourseNameKr.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "필수 항목을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            Double.parseDouble(txtCredit.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "학점은 숫자로 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void clearForm() {
        txtCourseCode.setText("");
        txtCourseNameKr.setText("");
        txtCourseNameEn.setText("");
        txtCredit.setText("");
        txtRecommendedYear.setText("");
        cbCourseType.setSelectedIndex(0);
        table.clearSelection();
    }
}