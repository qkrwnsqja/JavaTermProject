package university.view;

import university.dao.DepartmentDAO;
import university.dao.ProfessorDAO;
import university.model.Department;
import university.model.Professor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

/**
 * 교수 관리 패널
 */
public class ProfessorPanel extends JPanel {

    private ProfessorDAO professorDAO;
    private DepartmentDAO departmentDAO;

    private JTable table;
    private DefaultTableModel tableModel;
    private String[] columnNames = {"교수ID", "이름", "학과", "직위", "연구실", "이메일"};

    private JTextField txtProfessorId, txtNameKr, txtNameEn, txtRrn;
    private JTextField txtOfficeLocation, txtOfficePhone, txtEmail;
    private JComboBox<Department> cbDept;
    private JComboBox<String> cbPosition;
    private JTextField txtSearch;

    public ProfessorPanel() {
        this.professorDAO = new ProfessorDAO();
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
        add(createSearchPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createFormPanel(), BorderLayout.EAST);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("교수 검색:");
        lblSearch.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnSearch = new JButton("검색");
        btnSearch.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnSearch.addActionListener(e -> searchProfessor());

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
                    String professorId = tableModel.getValueAt(row, 0).toString();
                    loadProfessor(professorId);
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

        JLabel titleLabel = new JLabel("교수 정보 입력");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        txtProfessorId = addFormField(panel, "교수 ID:");
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

        // 직위 콤보박스
        JLabel lblPosition = new JLabel("직위:");
        lblPosition.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        lblPosition.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbPosition = new JComboBox<>(new String[]{"교수", "부교수", "조교수", "명예교수"});
        cbPosition.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        cbPosition.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cbPosition.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblPosition);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(cbPosition);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        txtOfficeLocation = addFormField(panel, "연구실:");
        txtOfficePhone = addFormField(panel, "연구실 전화:");
        txtEmail = addFormField(panel, "이메일:");

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

        btnAdd.addActionListener(e -> addProfessor());
        btnUpdate.addActionListener(e -> updateProfessor());
        btnDelete.addActionListener(e -> deleteProfessor());
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
        List<Professor> list = professorDAO.selectAll();

        for (Professor professor : list) {
            Object[] row = {
                    professor.getProfessorId(),
                    professor.getNameKr(),
                    professor.getDeptName(),
                    professor.getPosition(),
                    professor.getOfficeLocation(),
                    professor.getEmail()
            };
            tableModel.addRow(row);
        }
    }

    private void searchProfessor() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        tableModel.setRowCount(0);
        List<Professor> list = professorDAO.searchByName(keyword);

        for (Professor professor : list) {
            Object[] row = {
                    professor.getProfessorId(),
                    professor.getNameKr(),
                    professor.getDeptName(),
                    professor.getPosition(),
                    professor.getOfficeLocation(),
                    professor.getEmail()
            };
            tableModel.addRow(row);
        }
    }

    private void loadProfessor(String professorId) {
        Professor professor = professorDAO.selectById(professorId);
        if (professor != null) {
            txtProfessorId.setText(professor.getProfessorId());
            txtNameKr.setText(professor.getNameKr());
            txtNameEn.setText(professor.getNameEn());
            txtRrn.setText(professor.getRrn());
            txtOfficeLocation.setText(professor.getOfficeLocation());
            txtOfficePhone.setText(professor.getOfficePhone());
            txtEmail.setText(professor.getEmail());
            cbPosition.setSelectedItem(professor.getPosition());

            for (int i = 0; i < cbDept.getItemCount(); i++) {
                Department dept = cbDept.getItemAt(i);
                if (dept.getDeptCode().equals(professor.getDeptCode())) {
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

    private void addProfessor() {
        if (!validateInput()) return;

        Professor professor = new Professor();
        professor.setProfessorId(txtProfessorId.getText().trim());
        professor.setNameKr(txtNameKr.getText().trim());
        professor.setNameEn(txtNameEn.getText().trim());
        professor.setRrn(txtRrn.getText().trim());
        professor.setDeptCode(((Department)cbDept.getSelectedItem()).getDeptCode());
        professor.setPosition((String)cbPosition.getSelectedItem());
        professor.setOfficeLocation(txtOfficeLocation.getText().trim());
        professor.setOfficePhone(txtOfficePhone.getText().trim());
        professor.setEmail(txtEmail.getText().trim());
        professor.setHireDate(new Date());

        if (professorDAO.insert(professor)) {
            JOptionPane.showMessageDialog(this, "교수가 등록되었습니다.");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "등록에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProfessor() {
        if (!validateInput()) return;

        Professor professor = new Professor();
        professor.setProfessorId(txtProfessorId.getText().trim());
        professor.setNameKr(txtNameKr.getText().trim());
        professor.setNameEn(txtNameEn.getText().trim());
        professor.setRrn(txtRrn.getText().trim());
        professor.setDeptCode(((Department)cbDept.getSelectedItem()).getDeptCode());
        professor.setPosition((String)cbPosition.getSelectedItem());
        professor.setOfficeLocation(txtOfficeLocation.getText().trim());
        professor.setOfficePhone(txtOfficePhone.getText().trim());
        professor.setEmail(txtEmail.getText().trim());
        professor.setHireDate(new Date());

        if (professorDAO.update(professor)) {
            JOptionPane.showMessageDialog(this, "교수 정보가 수정되었습니다.");
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "수정에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProfessor() {
        String professorId = txtProfessorId.getText().trim();
        if (professorId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "삭제할 교수를 선택하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (professorDAO.delete(professorId)) {
                JOptionPane.showMessageDialog(this, "교수가 삭제되었습니다.");
                clearForm();
                loadData();
            }
        }
    }

    private boolean validateInput() {
        if (txtProfessorId.getText().trim().isEmpty() || txtNameKr.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "필수 항목을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearForm() {
        txtProfessorId.setText("");
        txtNameKr.setText("");
        txtNameEn.setText("");
        txtRrn.setText("");
        txtOfficeLocation.setText("");
        txtOfficePhone.setText("");
        txtEmail.setText("");
        cbPosition.setSelectedIndex(0);
        if (cbDept.getItemCount() > 0) {
            cbDept.setSelectedIndex(0);
        }
        table.clearSelection();
    }
}