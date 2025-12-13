package university.view;

import university.dao.DepartmentDAO;
import university.model.Department;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 학과 관리 패널
 */
public class DepartmentPanel extends JPanel {

    private DepartmentDAO departmentDAO;

    // 테이블 관련
    private JTable table;
    private DefaultTableModel tableModel;
    private String[] columnNames = {"학과코드", "학과명", "단과대학", "사무실위치", "전화번호"};

    // 입력 필드
    private JTextField txtDeptCode;
    private JTextField txtDeptName;
    private JTextField txtCollegeName;
    private JTextField txtOfficeLocation;
    private JTextField txtOfficePhone;
    private JTextField txtSearch;

    public DepartmentPanel() {
        this.departmentDAO = new DepartmentDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initComponents();
        loadData();
    }

    private void initComponents() {
        // 상단 패널 (검색)
        JPanel topPanel = createSearchPanel();
        add(topPanel, BorderLayout.NORTH);

        // 중앙 패널 (테이블)
        JPanel centerPanel = createTablePanel();
        add(centerPanel, BorderLayout.CENTER);

        // 우측 패널 (입력 폼)
        JPanel rightPanel = createFormPanel();
        add(rightPanel, BorderLayout.EAST);
    }

    /**
     * 검색 패널 생성
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);

        JLabel lblSearch = new JLabel("학과명 검색:");
        lblSearch.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JButton btnSearch = new JButton("검색");
        btnSearch.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnSearch.addActionListener(e -> searchDepartment());

        JButton btnRefresh = new JButton("전체 조회");
        btnRefresh.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btnRefresh.addActionListener(e -> loadData());

        panel.add(lblSearch);
        panel.add(txtSearch);
        panel.add(btnSearch);
        panel.add(btnRefresh);

        return panel;
    }

    /**
     * 테이블 패널 생성
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // 테이블 모델 생성
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 셀 편집 불가
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 행 선택 이벤트
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    loadSelectedRow(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 입력 폼 패널 생성
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(236, 240, 241));
        panel.setPreferredSize(new Dimension(350, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 제목
        JLabel titleLabel = new JLabel("학과 정보 입력");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 입력 필드들
        txtDeptCode = addFormField(panel, "학과 코드:");
        txtDeptName = addFormField(panel, "학과명:");
        txtCollegeName = addFormField(panel, "단과대학:");
        txtOfficeLocation = addFormField(panel, "사무실 위치:");
        txtOfficePhone = addFormField(panel, "전화번호:");

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 버튼 패널
        JPanel buttonPanel = createButtonPanel();
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(buttonPanel);

        return panel;
    }

    /**
     * 폼 필드 추가 헬퍼 메서드
     */
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

    /**
     * 버튼 패널 생성
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JButton btnAdd = new JButton("등록");
        JButton btnUpdate = new JButton("수정");
        JButton btnDelete = new JButton("삭제");
        JButton btnClear = new JButton("초기화");

        // 버튼 스타일 설정
        Font buttonFont = new Font("맑은 고딕", Font.BOLD, 13);
        btnAdd.setFont(buttonFont);
        btnUpdate.setFont(buttonFont);
        btnDelete.setFont(buttonFont);
        btnClear.setFont(buttonFont);

        btnAdd.setBackground(new Color(46, 204, 113));
        btnUpdate.setBackground(new Color(52, 152, 219));
        btnDelete.setBackground(new Color(231, 76, 60));
        btnClear.setBackground(new Color(149, 165, 166));

        btnAdd.setForeground(Color.WHITE);
        btnUpdate.setForeground(Color.WHITE);
        btnDelete.setForeground(Color.WHITE);
        btnClear.setForeground(Color.WHITE);

        // 버튼 색상 확실하게 표시
        btnAdd.setOpaque(true);
        btnAdd.setBorderPainted(false);
        btnUpdate.setOpaque(true);
        btnUpdate.setBorderPainted(false);
        btnDelete.setOpaque(true);
        btnDelete.setBorderPainted(false);
        btnClear.setOpaque(true);
        btnClear.setBorderPainted(false);

        // 버튼 이벤트
        btnAdd.addActionListener(e -> addDepartment());
        btnUpdate.addActionListener(e -> updateDepartment());
        btnDelete.addActionListener(e -> deleteDepartment());
        btnClear.addActionListener(e -> clearForm());

        panel.add(btnAdd);
        panel.add(btnUpdate);
        panel.add(btnDelete);
        panel.add(btnClear);

        return panel;
    }

    /**
     * 데이터 로드
     */
    private void loadData() {
        tableModel.setRowCount(0); // 기존 데이터 삭제

        List<Department> list = departmentDAO.selectAll();
        for (Department dept : list) {
            Object[] row = {
                    dept.getDeptCode(),
                    dept.getDeptName(),
                    dept.getCollegeName(),
                    dept.getOfficeLocation(),
                    dept.getOfficePhone()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * 학과 검색
     */
    private void searchDepartment() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }

        tableModel.setRowCount(0);
        List<Department> list = departmentDAO.searchByName(keyword);

        for (Department dept : list) {
            Object[] row = {
                    dept.getDeptCode(),
                    dept.getDeptName(),
                    dept.getCollegeName(),
                    dept.getOfficeLocation(),
                    dept.getOfficePhone()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * 선택된 행 데이터 로드
     */
    private void loadSelectedRow(int row) {
        txtDeptCode.setText(tableModel.getValueAt(row, 0).toString());
        txtDeptName.setText(tableModel.getValueAt(row, 1).toString());
        txtCollegeName.setText(tableModel.getValueAt(row, 2) != null ?
                tableModel.getValueAt(row, 2).toString() : "");
        txtOfficeLocation.setText(tableModel.getValueAt(row, 3) != null ?
                tableModel.getValueAt(row, 3).toString() : "");
        txtOfficePhone.setText(tableModel.getValueAt(row, 4) != null ?
                tableModel.getValueAt(row, 4).toString() : "");
    }

    /**
     * 학과 등록
     */
    private void addDepartment() {
        if (!validateInput()) return;

        Department dept = new Department();
        dept.setDeptCode(txtDeptCode.getText().trim());
        dept.setDeptName(txtDeptName.getText().trim());
        dept.setCollegeName(txtCollegeName.getText().trim());
        dept.setOfficeLocation(txtOfficeLocation.getText().trim());
        dept.setOfficePhone(txtOfficePhone.getText().trim());

        if (departmentDAO.insert(dept)) {
            JOptionPane.showMessageDialog(this, "학과가 등록되었습니다.",
                    "성공", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "학과 등록에 실패했습니다.",
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 학과 수정
     */
    private void updateDepartment() {
        if (!validateInput()) return;

        Department dept = new Department();
        dept.setDeptCode(txtDeptCode.getText().trim());
        dept.setDeptName(txtDeptName.getText().trim());
        dept.setCollegeName(txtCollegeName.getText().trim());
        dept.setOfficeLocation(txtOfficeLocation.getText().trim());
        dept.setOfficePhone(txtOfficePhone.getText().trim());

        if (departmentDAO.update(dept)) {
            JOptionPane.showMessageDialog(this, "학과 정보가 수정되었습니다.",
                    "성공", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "학과 수정에 실패했습니다.",
                    "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 학과 삭제
     */
    private void deleteDepartment() {
        String deptCode = txtDeptCode.getText().trim();
        if (deptCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "삭제할 학과를 선택하세요.",
                    "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "정말 삭제하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (departmentDAO.delete(deptCode)) {
                JOptionPane.showMessageDialog(this, "학과가 삭제되었습니다.",
                        "성공", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "학과 삭제에 실패했습니다.",
                        "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 입력 검증
     */
    private boolean validateInput() {
        if (txtDeptCode.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "학과 코드를 입력하세요.",
                    "경고", JOptionPane.WARNING_MESSAGE);
            txtDeptCode.requestFocus();
            return false;
        }

        if (txtDeptName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "학과명을 입력하세요.",
                    "경고", JOptionPane.WARNING_MESSAGE);
            txtDeptName.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * 폼 초기화
     */
    private void clearForm() {
        txtDeptCode.setText("");
        txtDeptName.setText("");
        txtCollegeName.setText("");
        txtOfficeLocation.setText("");
        txtOfficePhone.setText("");
        table.clearSelection();
    }
}