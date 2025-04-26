import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable teacherTable, studentTable, courseTable;
    private DefaultTableModel teacherModel, studentModel, courseModel;

    public AdminDashboard() {
        setTitle("Admin Dashboard - Attendance Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        
        // Teachers Tab
        JPanel teachersPanel = new JPanel(new BorderLayout());
        teacherModel = new DefaultTableModel(new String[]{"ID", "Name", "Email"}, 0);
        teacherTable = new JTable(teacherModel);
        teachersPanel.add(new JScrollPane(teacherTable), BorderLayout.CENTER);
        
        JPanel teacherButtonPanel = new JPanel();
        JButton addTeacherBtn = new JButton("Add Teacher");
        JButton editTeacherBtn = new JButton("Edit Teacher");
        JButton deleteTeacherBtn = new JButton("Delete Teacher");
        teacherButtonPanel.add(addTeacherBtn);
        teacherButtonPanel.add(editTeacherBtn);
        teacherButtonPanel.add(deleteTeacherBtn);
        teachersPanel.add(teacherButtonPanel, BorderLayout.SOUTH);
        
        // Students Tab
        JPanel studentsPanel = new JPanel(new BorderLayout());
        studentModel = new DefaultTableModel(new String[]{"Roll No", "Name", "Batch", "Email"}, 0);
        studentTable = new JTable(studentModel);
        studentsPanel.add(new JScrollPane(studentTable), BorderLayout.CENTER);
        
        JPanel studentButtonPanel = new JPanel();
        JButton addStudentBtn = new JButton("Add Student");
        JButton editStudentBtn = new JButton("Edit Student");
        JButton deleteStudentBtn = new JButton("Delete Student");
        studentButtonPanel.add(addStudentBtn);
        studentButtonPanel.add(editStudentBtn);
        studentButtonPanel.add(deleteStudentBtn);
        studentsPanel.add(studentButtonPanel, BorderLayout.SOUTH);
        
        // Courses Tab
        JPanel coursesPanel = new JPanel(new BorderLayout());
        courseModel = new DefaultTableModel(new String[]{"Course ID", "Course Name", "Teacher", "Batch"}, 0);
        courseTable = new JTable(courseModel);
        coursesPanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        
        JPanel courseButtonPanel = new JPanel();
        JButton addCourseBtn = new JButton("Add Course");
        JButton editCourseBtn = new JButton("Edit Course");
        JButton deleteCourseBtn = new JButton("Delete Course");
        courseButtonPanel.add(addCourseBtn);
        courseButtonPanel.add(editCourseBtn);
        courseButtonPanel.add(deleteCourseBtn);
        coursesPanel.add(courseButtonPanel, BorderLayout.SOUTH);
        
        // Add tabs
        tabbedPane.addTab("Teachers", teachersPanel);
        tabbedPane.addTab("Students", studentsPanel);
        tabbedPane.addTab("Courses", coursesPanel);
        
        add(tabbedPane);
        
        // Load initial data
        loadTeachers();
        loadStudents();
        loadCourses();
        
        // Add action listeners
        addTeacherBtn.addActionListener(e -> showAddTeacherDialog());
        addStudentBtn.addActionListener(e -> showAddStudentDialog());
        addCourseBtn.addActionListener(e -> showAddCourseDialog());
        
        // Add action listeners for edit and delete buttons
        editTeacherBtn.addActionListener(e -> showEditTeacherDialog());
        deleteTeacherBtn.addActionListener(e -> deleteTeacher());
        editStudentBtn.addActionListener(e -> showEditStudentDialog());
        deleteStudentBtn.addActionListener(e -> deleteStudent());
        editCourseBtn.addActionListener(e -> showEditCourseDialog());
        deleteCourseBtn.addActionListener(e -> deleteCourse());
    }
    
    private void loadTeachers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM teachers");
            teacherModel.setRowCount(0);
            while (rs.next()) {
                teacherModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading teachers: " + ex.getMessage());
        }
    }
    
    private void loadStudents() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");
            studentModel.setRowCount(0);
            while (rs.next()) {
                studentModel.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    rs.getString("batch"),
                    rs.getString("email")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
        }
    }
    
    private void loadCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT c.*, t.name as teacher_name FROM courses c LEFT JOIN teachers t ON c.teacher_id = t.id");
            courseModel.setRowCount(0);
            while (rs.next()) {
                courseModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("teacher_name"),
                    rs.getString("batch")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage());
        }
    }
    
    private void showAddTeacherDialog() {
        JDialog dialog = new JDialog(this, "Add Teacher", true);
        dialog.setLayout(new GridLayout(4, 2));
        
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        
        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        dialog.add(new JLabel("Username:"));
        dialog.add(usernameField);
        dialog.add(new JLabel("Password:"));
        dialog.add(passwordField);
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // First insert into teachers table
                PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO teachers (name, email) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, nameField.getText());
                pstmt.setString(2, emailField.getText());
                pstmt.executeUpdate();
                
                // Get the generated teacher ID
                ResultSet rs = pstmt.getGeneratedKeys();
                int teacherId = 0;
                if (rs.next()) {
                    teacherId = rs.getInt(1);
                }
                
                // Then insert into users table with the teacher_id
                pstmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role, teacher_id) VALUES (?, ?, 'teacher', ?)");
                pstmt.setString(1, usernameField.getText());
                pstmt.setString(2, new String(passwordField.getPassword()));
                pstmt.setInt(3, teacherId);
                pstmt.executeUpdate();
                
                loadTeachers();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding teacher: " + ex.getMessage());
            }
        });
        
        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void showAddStudentDialog() {
        JDialog dialog = new JDialog(this, "Add Student", true);
        dialog.setLayout(new GridLayout(5, 2));
        
        JTextField rollNoField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField batchField = new JTextField();
        JTextField emailField = new JTextField();
        
        dialog.add(new JLabel("Roll No:"));
        dialog.add(rollNoField);
        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Batch:"));
        dialog.add(batchField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO students (roll_no, name, batch, email) VALUES (?, ?, ?, ?)");
                pstmt.setString(1, rollNoField.getText());
                pstmt.setString(2, nameField.getText());
                pstmt.setString(3, batchField.getText());
                pstmt.setString(4, emailField.getText());
                pstmt.executeUpdate();
                loadStudents();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding student: " + ex.getMessage());
            }
        });
        
        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void showAddCourseDialog() {
        JDialog dialog = new JDialog(this, "Add Course", true);
        dialog.setLayout(new GridLayout(4, 2));
        
        JTextField nameField = new JTextField();
        JComboBox<String> teacherCombo = new JComboBox<>();
        JTextField batchField = new JTextField();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM teachers");
            while (rs.next()) {
                teacherCombo.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Error loading teachers: " + ex.getMessage());
        }
        
        dialog.add(new JLabel("Course Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Teacher:"));
        dialog.add(teacherCombo);
        dialog.add(new JLabel("Batch:"));
        dialog.add(batchField);
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO courses (name, teacher_id, batch) VALUES (?, ?, ?)");
                pstmt.setString(1, nameField.getText());
                pstmt.setInt(2, teacherCombo.getSelectedIndex() + 1);
                pstmt.setString(3, batchField.getText());
                pstmt.executeUpdate();
                loadCourses();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding course: " + ex.getMessage());
            }
        });
        
        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditTeacherDialog() {
        int selectedRow = teacherTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a teacher to edit");
            return;
        }

        JDialog dialog = new JDialog(this, "Edit Teacher", true);
        dialog.setLayout(new GridLayout(4, 2));

        int teacherId = (int) teacherModel.getValueAt(selectedRow, 0);
        JTextField nameField = new JTextField((String) teacherModel.getValueAt(selectedRow, 1));
        JTextField emailField = new JTextField((String) teacherModel.getValueAt(selectedRow, 2));

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE teachers SET name = ?, email = ? WHERE id = ?");
                pstmt.setString(1, nameField.getText());
                pstmt.setString(2, emailField.getText());
                pstmt.setInt(3, teacherId);
                pstmt.executeUpdate();
                loadTeachers();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating teacher: " + ex.getMessage());
            }
        });

        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteTeacher() {
        int selectedRow = teacherTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a teacher to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this teacher?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                int teacherId = (int) teacherModel.getValueAt(selectedRow, 0);
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM teachers WHERE id = ?");
                pstmt.setInt(1, teacherId);
                pstmt.executeUpdate();
                loadTeachers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting teacher: " + ex.getMessage());
            }
        }
    }

    private void showEditStudentDialog() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit");
            return;
        }

        JDialog dialog = new JDialog(this, "Edit Student", true);
        dialog.setLayout(new GridLayout(5, 2));

        String rollNo = (String) studentModel.getValueAt(selectedRow, 0);
        JTextField nameField = new JTextField((String) studentModel.getValueAt(selectedRow, 1));
        JTextField batchField = new JTextField((String) studentModel.getValueAt(selectedRow, 2));
        JTextField emailField = new JTextField((String) studentModel.getValueAt(selectedRow, 3));

        dialog.add(new JLabel("Roll No:"));
        dialog.add(new JLabel(rollNo));
        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Batch:"));
        dialog.add(batchField);
        dialog.add(new JLabel("Email:"));
        dialog.add(emailField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE students SET name = ?, batch = ?, email = ? WHERE roll_no = ?");
                pstmt.setString(1, nameField.getText());
                pstmt.setString(2, batchField.getText());
                pstmt.setString(3, emailField.getText());
                pstmt.setString(4, rollNo);
                pstmt.executeUpdate();
                loadStudents();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating student: " + ex.getMessage());
            }
        });

        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this student?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String rollNo = (String) studentModel.getValueAt(selectedRow, 0);
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM students WHERE roll_no = ?");
                pstmt.setString(1, rollNo);
                pstmt.executeUpdate();
                loadStudents();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting student: " + ex.getMessage());
            }
        }
    }

    private void showEditCourseDialog() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit");
            return;
        }

        JDialog dialog = new JDialog(this, "Edit Course", true);
        dialog.setLayout(new GridLayout(4, 2));

        int courseId = (int) courseModel.getValueAt(selectedRow, 0);
        JTextField nameField = new JTextField((String) courseModel.getValueAt(selectedRow, 1));
        JComboBox<String> teacherCombo = new JComboBox<>();
        JTextField batchField = new JTextField((String) courseModel.getValueAt(selectedRow, 3));

        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name FROM teachers");
            while (rs.next()) {
                teacherCombo.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Error loading teachers: " + ex.getMessage());
        }

        dialog.add(new JLabel("Course Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Teacher:"));
        dialog.add(teacherCombo);
        dialog.add(new JLabel("Batch:"));
        dialog.add(batchField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE courses SET name = ?, teacher_id = ?, batch = ? WHERE id = ?");
                pstmt.setString(1, nameField.getText());
                pstmt.setInt(2, teacherCombo.getSelectedIndex() + 1);
                pstmt.setString(3, batchField.getText());
                pstmt.setInt(4, courseId);
                pstmt.executeUpdate();
                loadCourses();
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error updating course: " + ex.getMessage());
            }
        });

        dialog.add(saveButton);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteCourse() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this course?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                int courseId = (int) courseModel.getValueAt(selectedRow, 0);
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM courses WHERE id = ?");
                pstmt.setInt(1, courseId);
                pstmt.executeUpdate();
                loadCourses();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting course: " + ex.getMessage());
            }
        }
    }
} 