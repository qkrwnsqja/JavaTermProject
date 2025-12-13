package university.view;

import university.dao.DepartmentDAO;
import university.dao.StudentDAO;
import university.model.Department;
import university.model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

/**
 * 학생 관리 패널
 */
public class StudentManagementPanel extends JPanel {

    private StudentDAO studentDAO;
    private DepartmentDAO departmentDAO;

    // 테이블
    private JTable table;
    private DefaultTableModel tableModel;
    private String[] columnNames = {"학번", "이름", "학과", "학적상태", "전화번호", "이메일"};

    // 입력 필드
    private JTextField txtStudentId, txtNameKr, txtNameEn, txtRrn;
    private JTextField txtPhone, txtEmail, txtAddress;
    private JComboBox<Department> cbDept;
    private JComboBox<String> cbStatus;
    private JTextField txtSearch;

    public StudentManagementPanel() {
        this.studentDAO = new StudentDAO();
        this.departmentDAO = new DepartmentDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        loadData();
        loadDepartments();
    }

    /**
     * 패널이 보여질 때 호출 (데이터 새로고침)
     */
    public void refreshData() {
        loadData();
        loadDepartments();
    }

    private void initComponents() {
        // 상단 검색 패널
        add(createSearchPanel(), BorderLayout.NORTH);

        // 중앙 테이블
        add(createTablePanel(), BorderLayout.CENTER);

        // 우측 입력 폼
        add(createFormPanel(), BorderLayout.EAST);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("학생 검색:");
        lblSearch.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnSearch = new JButton("검색");
        btnSearch.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnSearch.addActionListener(e -> searchStudent());

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
                    String studentId = tableModel.getValueAt(row, 0).toString();
                    loadStudent(studentId);
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

        JLabel titleLabel = new JLabel("학생 정보 입력");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        txtStudentId = addFormField(panel, "학번:");
        txtNameKr = addFormField(panel, "한글 이름:");
        txtNameEn = addFormField(panel, "영문 이름:");
        txtRrn = addFormField(panel, "주민등록번호:");

        // 학과 콤보박스
        JLabel lblDept = new JLabel("학과:");
        lblDept.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        lblDept.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbDept = new JComboBox<>();
        cbDept.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        cbDept.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbDept.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblDept);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(cbDept);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // 학적 상태 콤보박스
        JLabel lblStatus = new JLabel("학적 상태:");
        lblStatus.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbStatus = new JComboBox<>(new String[]{"ENROLLED", "LEAVE", "WITHDRAWN", "GRADUATED"});
        cbStatus.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblStatus);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(cbStatus);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        txtPhone = addFormField(panel, "전화번호:");
        txtEmail = addFormField(panel, "이메일:");
        txtAddress = addFormField(panel, "주소:");

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
        JButton btnDelete = createStyledButton("삭제", new Color(231, 76, 60));
        JButton btnClear = createStyledButton("초기화", new Color(149, 165, 166));

        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
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
        List<Student> list = studentDAO.selectAll();

        for (Student student : list) {
            Object[] row = {
                    student.getStudentId(),
                    student.getNameKr(),
                    student.getDeptName(),
                    student.getStatusKorean(),
                    student.getPhone(),
                    student.getEmail()
            };
            tableModel.addRow(row);
        }
    }

    private void searchStudent() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        tableModel.setRowCount(0);
        List<Student> list = studentDAO.searchByName(keyword);

        for (Student student : list) {
            Object[] row = {
                    student.getStudentId(),
                    student.getNameKr(),
                    student.getDeptName(),
                    student.getStatusKorean(),
                    student.getPhone(),
                    student.getEmail()
            };
            tableModel.addRow(row);
        }
    }

    private void loadStudent(String studentId) {
        Student student = studentDAO.selectById(studentId);
        if (student != null) {
            txtStudentId.setText(student.getStudentId());
            txtNameKr.setText(student.getNameKr());
            txtNameEn.setText(student.getNameEn());
            txtRrn.setText(student.getRrn());
            txtPhone.setText(student.getPhone());
            txtEmail.setText(student.getEmail());
            txtAddress.setText(student.getAddress());
            cbStatus.setSelectedItem(student.getStatus());

            // 학과 선택
            for (int i = 0; i < cbDept.getItemCount(); i++) {
                Department dept = cbDept.getItemAt(i);
                if (dept.getDeptCode().equals(student.getDeptCode())) {
                    cbDept.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void loadDepartments() {
        cbDept.removeAllItems();
        List<Department> list = departmentDAO.selectAll();
        for (Department dept : list) {
            cbDept.addItem(dept);
        }
    }

    private void addStudent() {
        if (!validateInput()) return;

        Student student = new Student();
        student.setStudentId(txtStudentId.getText().trim());
        student.setNameKr(txtNameKr.getText().trim());
        student.setNameEn(txtNameEn.getText().trim());
        student.setRrn(txtRrn.getText().trim());
        student.setDeptCode(((Department)cbDept.getSelectedItem()).getDeptCode());
        student.setStatus((String)cbStatus.getSelectedItem());
        student.setAdmissionDate(new Date());
        student.setPhone(txtPhone.getText().trim());
        student.setEmail(txtEmail.getText().trim());
        student.setAddress(txtAddress.getText().trim());
        student.setNationality("대한민국");

        if (studentDAO.insert(student)) {
            JOptionPane.showMessageDialog(this, "학생이 등록되었습니다.");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "학생 등록에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStudent() {
        if (!validateInput()) return;

        Student student = new Student();
        student.setStudentId(txtStudentId.getText().trim());
        student.setNameKr(txtNameKr.getText().trim());
        student.setNameEn(txtNameEn.getText().trim());
        student.setRrn(txtRrn.getText().trim());
        student.setDeptCode(((Department)cbDept.getSelectedItem()).getDeptCode());
        student.setStatus((String)cbStatus.getSelectedItem());
        student.setAdmissionDate(new Date());
        student.setPhone(txtPhone.getText().trim());
        student.setEmail(txtEmail.getText().trim());
        student.setAddress(txtAddress.getText().trim());
        student.setNationality("대한민국");

        if (studentDAO.update(student)) {
            JOptionPane.showMessageDialog(this, "학생 정보가 수정되었습니다.");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "수정에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        String studentId = txtStudentId.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "삭제할 학생을 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (studentDAO.delete(studentId)) {
                JOptionPane.showMessageDialog(this, "학생이 삭제되었습니다.");
                clearForm();
                loadData();
            }
        }
    }

    private boolean validateInput() {
        if (txtStudentId.getText().trim().isEmpty() || txtNameKr.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "필수 항목을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtStudentId.setText("");
        txtNameKr.setText("");
        txtNameEn.setText("");
        txtRrn.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        cbStatus.setSelectedIndex(0);
        if (cbDept.getItemCount() > 0) {
            cbDept.setSelectedIndex(0);
        }
        table.clearSelection();
    }
}