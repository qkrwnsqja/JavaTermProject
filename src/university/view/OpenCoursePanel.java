package university.view;

import university.dao.CourseDAO;
import university.dao.OpenCourseDAO;
import university.dao.ProfessorDAO;
import university.model.Course;
import university.model.OpenCourse;
import university.model.Professor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 개설강좌 관리 패널
 */
public class OpenCoursePanel extends JPanel {

    private OpenCourseDAO openCourseDAO;
    private CourseDAO courseDAO;
    private ProfessorDAO professorDAO;

    private JTable table;
    private DefaultTableModel tableModel;
    private String[] columnNames = {"개설ID", "학년도", "학기", "과목명", "학점", "분반", "교수", "정원", "신청인원", "강의실"};

    private JTextField txtYear, txtSection, txtRoom, txtCapacity;
    private JComboBox<String> cbTerm;
    private JComboBox<Course> cbCourse;
    private JComboBox<Professor> cbProfessor;
    private JTextField txtSearch;

    public OpenCoursePanel() {
        this.openCourseDAO = new OpenCourseDAO();
        this.courseDAO = new CourseDAO();
        this.professorDAO = new ProfessorDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        loadCourses();
        loadProfessors();
        loadData();
    }

    /**
     * 패널이 보여질 때 호출 (데이터 새로고침)
     */
    public void refreshData() {
        loadCourses();
        loadProfessors();
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

        JLabel lblYear = new JLabel("학년도:");
        lblYear.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        JTextField txtSearchYear = new JTextField("2025", 8);
        txtSearchYear.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JLabel lblTerm = new JLabel("학기:");
        lblTerm.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        JComboBox<String> cbSearchTerm = new JComboBox<>(new String[]{"1학기", "2학기", "여름학기", "겨울학기"});
        cbSearchTerm.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnSearch = new JButton("조회");
        btnSearch.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnSearch.addActionListener(e -> {
            int year = Integer.parseInt(txtSearchYear.getText().trim());
            String term = (String)cbSearchTerm.getSelectedItem();
            loadDataByTerm(year, term);
        });

        JButton btnRefresh = new JButton("전체 조회");
        btnRefresh.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> loadData());

        panel.add(lblYear);
        panel.add(txtSearchYear);
        panel.add(lblTerm);
        panel.add(cbSearchTerm);
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
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    int openCourseId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                    loadOpenCourse(openCourseId);
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

        JLabel titleLabel = new JLabel("개설강좌 등록");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        txtYear = addFormField(panel, "학년도:");
        txtYear.setText("2025");

        // 학기 콤보박스
        JLabel lblTerm = new JLabel("학기:");
        lblTerm.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        lblTerm.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbTerm = new JComboBox<>(new String[]{"1학기", "2학기", "여름학기", "겨울학기"});
        cbTerm.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        cbTerm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbTerm.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTerm);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(cbTerm);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 과목 콤보박스
        JLabel lblCourse = new JLabel("과목:");
        lblCourse.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        lblCourse.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbCourse = new JComboBox<>();
        cbCourse.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        cbCourse.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbCourse.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblCourse);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(cbCourse);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        txtSection = addFormField(panel, "분반:");

        // 교수 콤보박스
        JLabel lblProf = new JLabel("담당교수:");
        lblProf.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        lblProf.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbProfessor = new JComboBox<>();
        cbProfessor.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        cbProfessor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbProfessor.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblProf);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(cbProfessor);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        txtRoom = addFormField(panel, "강의실:");
        txtCapacity = addFormField(panel, "수강정원:");

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
        JButton btnCancel = createStyledButton("폐강", new Color(231, 76, 60));
        JButton btnClear = createStyledButton("초기화", new Color(149, 165, 166));

        btnAdd.addActionListener(e -> addOpenCourse());
        btnUpdate.addActionListener(e -> updateOpenCourse());
        btnCancel.addActionListener(e -> cancelOpenCourse());
        btnClear.addActionListener(e -> clearForm());

        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnCancel);
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
        loadDataByTerm(2025, "2학기");
    }

    private void loadDataByTerm(int year, String term) {
        tableModel.setRowCount(0);
        List<OpenCourse> list = openCourseDAO.selectByYearAndTerm(year, term);

        for (OpenCourse oc : list) {
            Object[] row = {
                    oc.getOpenCourseId(),
                    oc.getYear(),
                    oc.getTerm(),
                    oc.getCourseNameKr(),
                    oc.getCredit(),
                    oc.getSection(),
                    oc.getProfessorName(),
                    oc.getCapacity(),
                    oc.getEnrolledCount(),
                    oc.getRoom()
            };
            tableModel.addRow(row);
        }
    }

    private void loadOpenCourse(int openCourseId) {
        OpenCourse oc = openCourseDAO.selectById(openCourseId);
        if (oc != null) {
            txtYear.setText(String.valueOf(oc.getYear()));
            cbTerm.setSelectedItem(oc.getTerm());
            txtSection.setText(oc.getSection());
            txtRoom.setText(oc.getRoom());
            txtCapacity.setText(String.valueOf(oc.getCapacity()));

            // 과목 선택
            for (int i = 0; i < cbCourse.getItemCount(); i++) {
                Course course = cbCourse.getItemAt(i);
                if (course.getCourseCode().equals(oc.getCourseCode())) {
                    cbCourse.setSelectedIndex(i);
                    break;
                }
            }

            // 교수 선택
            for (int i = 0; i < cbProfessor.getItemCount(); i++) {
                Professor prof = cbProfessor.getItemAt(i);
                if (prof.getProfessorId().equals(oc.getProfessorId())) {
                    cbProfessor.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void loadCourses() {
        cbCourse.removeAllItems();
        List<Course> list = courseDAO.selectAll();
        for (Course course : list) {
            cbCourse.addItem(course);
        }
    }

    private void loadProfessors() {
        cbProfessor.removeAllItems();
        List<Professor> list = professorDAO.selectAll();
        for (Professor prof : list) {
            cbProfessor.addItem(prof);
        }
    }

    private void addOpenCourse() {
        if (!validateInput()) return;

        OpenCourse oc = new OpenCourse();
        oc.setYear(Integer.parseInt(txtYear.getText().trim()));
        oc.setTerm((String)cbTerm.getSelectedItem());
        oc.setCourseCode(((Course)cbCourse.getSelectedItem()).getCourseCode());
        oc.setSection(txtSection.getText().trim());
        oc.setProfessorId(((Professor)cbProfessor.getSelectedItem()).getProfessorId());
        oc.setRoom(txtRoom.getText().trim());
        oc.setCapacity(Integer.parseInt(txtCapacity.getText().trim()));
        oc.setEnrolledCount(0);
        oc.setIsCanceled("N");

        if (openCourseDAO.insert(oc)) {
            JOptionPane.showMessageDialog(this, "개설강좌가 등록되었습니다.");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "등록에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOpenCourse() {
        JOptionPane.showMessageDialog(this, "선택된 개설강좌의 수정 기능은 추후 구현 예정입니다.");
    }

    private void cancelOpenCourse() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "폐강할 강좌를 선택하세요.");
            return;
        }

        int openCourseId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "정말 폐강하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (openCourseDAO.cancel(openCourseId)) {
                JOptionPane.showMessageDialog(this, "강좌가 폐강되었습니다.");
                loadData();
            }
        }
    }

    private boolean validateInput() {
        try {
            Integer.parseInt(txtYear.getText().trim());
            Integer.parseInt(txtCapacity.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "숫자 형식이 올바르지 않습니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtYear.setText("2025");
        cbTerm.setSelectedIndex(0);
        txtSection.setText("");
        txtRoom.setText("");
        txtCapacity.setText("");
        if (cbCourse.getItemCount() > 0) cbCourse.setSelectedIndex(0);
        if (cbProfessor.getItemCount() > 0) cbProfessor.setSelectedIndex(0);
        table.clearSelection();
    }
}