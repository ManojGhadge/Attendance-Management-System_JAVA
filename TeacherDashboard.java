import javax.swing.*;
import java.awt.*;
import java.sql.*;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import javax.swing.SpinnerDateModel;
import java.io.FileWriter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.ListSelectionModel;
import java.io.FileOutputStream;

public class TeacherDashboard extends JFrame {
    private JTabbedPane tabbedPane;
    private JTable attendanceTable;
    private DefaultTableModel attendanceModel;
    private JComboBox<String> courseCombo;
    private JComboBox<String> reportCourseCombo;
    private int teacherId;
    private String username;
    private JLabel statusLabel;
    private JProgressBar attendanceProgressBar;
    private JSpinner dateSpinner;
    private JPanel buttonPanel;
    private JTextField startDateField;
    private JTextField endDateField;

    public TeacherDashboard(String username) {
        this.username = username;
        setTitle("Teacher Dashboard - Attendance Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Get teacher ID from username
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT teacher_id FROM users WHERE username = ?");
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                teacherId = rs.getInt("teacher_id");
            } else {
                JOptionPane.showMessageDialog(this, "Teacher ID not found!");
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error getting teacher ID: " + ex.getMessage());
            return;
        }

        // Create main panel with background color
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));

        tabbedPane = new JTabbedPane();
        
        // Mark Attendance Tab
        JPanel markAttendancePanel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        courseCombo = new JComboBox<>();
        courseCombo.setPreferredSize(new Dimension(200, 30));
        topPanel.add(new JLabel("Select Course:"));
        topPanel.add(courseCombo);
        
        // Add date spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(new Date());
        topPanel.add(new JLabel("Date:"));
        topPanel.add(dateSpinner);
        
        JButton markAttendanceBtn = new JButton("Mark Attendance");
        try {
            ImageIcon icon = new ImageIcon("images/attendance_icon.png");
            markAttendanceBtn.setIcon(icon);
        } catch (Exception e) {
            // Continue without icon if not found
        }
        topPanel.add(markAttendanceBtn);
        
        // Add edit button
        JButton editAttendanceBtn = new JButton("Edit Attendance");
        try {
            ImageIcon icon = new ImageIcon("images/edit_icon.png");
            editAttendanceBtn.setIcon(icon);
        } catch (Exception e) {
            // Continue without icon if not found
        }
        topPanel.add(editAttendanceBtn);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(Color.BLUE);
        topPanel.add(statusLabel);
        
        markAttendancePanel.add(topPanel, BorderLayout.NORTH);
        
        attendanceModel = new DefaultTableModel(new String[]{"Roll No", "Name", "Status", "Late"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2 || columnIndex == 3) return Boolean.class;
                return String.class;
            }
        };
        attendanceTable = new JTable(attendanceModel);
        attendanceTable.setRowHeight(30);
        markAttendancePanel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);
        
        // View Reports Tab
        JPanel viewReportsPanel = new JPanel(new BorderLayout());
        
        JPanel reportControls = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        reportCourseCombo = new JComboBox<>();
        reportCourseCombo.setPreferredSize(new Dimension(200, 30));
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        
        // Create button panel with icons
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton generateReportBtn = new JButton("Generate Report");
        try {
            ImageIcon icon = new ImageIcon("images/report_icon.png");
            // Scale down the icon
            Image scaledImage = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            generateReportBtn.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            // Continue without icon if not found
        }
        generateReportBtn.setToolTipText("Generate attendance report for selected date range");
        
        JButton lowAttendanceBtn = new JButton("Low Attendance Report");
        try {
            ImageIcon icon = new ImageIcon("images/warning_icon.png");
            // Scale down the icon
            Image scaledImage = icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            lowAttendanceBtn.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            // Continue without icon if not found
        }
        lowAttendanceBtn.setToolTipText("View students with attendance below 75%");
        
        // Add hover effects
        generateReportBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                generateReportBtn.setBackground(new Color(200, 230, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                generateReportBtn.setBackground(UIManager.getColor("Button.background"));
            }
        });
        
        lowAttendanceBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lowAttendanceBtn.setBackground(new Color(255, 200, 200));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lowAttendanceBtn.setBackground(UIManager.getColor("Button.background"));
            }
        });
        
        buttonPanel.add(generateReportBtn);
        buttonPanel.add(lowAttendanceBtn);
        
        // First row
        gbc.gridx = 0; gbc.gridy = 0;
        reportControls.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        reportControls.add(reportCourseCombo, gbc);
        
        // Second row
        gbc.gridx = 0; gbc.gridy = 1;
        reportControls.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        reportControls.add(startDateField, gbc);
        
        // Third row
        gbc.gridx = 0; gbc.gridy = 2;
        reportControls.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        reportControls.add(endDateField, gbc);
        
        // Fourth row - buttons
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        reportControls.add(buttonPanel, gbc);
        
        // Add info label
        JLabel infoLabel = new JLabel("<html><div style='text-align: center;'>" +
            "Attendance Percentage = (Present + Late Days) / Total Classes × 100<br>" +
            "Low Attendance = Below 75% in the last 30 days</div></html>");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4;
        reportControls.add(infoLabel, gbc);
        
        DefaultTableModel reportModel = new DefaultTableModel(new String[]{"Roll No", "Name", "Date", "Status", "Attendance %"}, 0);
        JTable reportTable = new JTable(reportModel);
        reportTable.setRowHeight(30);
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Set preferred column widths
        reportTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Roll No
        reportTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Name
        reportTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Date
        reportTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
        reportTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Attendance %
        
        // Improve table appearance
        reportTable.setFillsViewportHeight(true);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.setGridColor(new Color(220, 220, 220));
        reportTable.setShowGrid(true);
        
        // Create a scroll pane with preferred size
        JScrollPane reportScrollPane = new JScrollPane(reportTable);
        reportScrollPane.setPreferredSize(new Dimension(800, 400));
        reportScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Create a panel for the progress bar with improved appearance
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        progressPanel.setMaximumSize(new Dimension(800, 30));
        
        attendanceProgressBar = new JProgressBar(0, 100);
        attendanceProgressBar.setStringPainted(true);
        attendanceProgressBar.setPreferredSize(new Dimension(200, 15));
        attendanceProgressBar.setMaximumSize(new Dimension(200, 15));
        attendanceProgressBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        attendanceProgressBar.setBackground(new Color(240, 240, 240));
        
        JLabel progressLabel = new JLabel("Overall Attendance: ");
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        
        progressPanel.add(progressLabel);
        progressPanel.add(attendanceProgressBar);
        
        // Create a container panel for the report section
        JPanel reportContainer = new JPanel(new BorderLayout());
        reportContainer.add(reportControls, BorderLayout.NORTH);
        
        // Create a panel for the report table and progress bar
        JPanel reportTablePanel = new JPanel(new BorderLayout());
        reportTablePanel.add(reportScrollPane, BorderLayout.CENTER);
        reportTablePanel.add(progressPanel, BorderLayout.SOUTH);
        
        reportContainer.add(reportTablePanel, BorderLayout.CENTER);
        
        viewReportsPanel.add(reportContainer, BorderLayout.CENTER);
        
        // Download Reports Tab
        JPanel downloadReportsPanel = new JPanel(new BorderLayout());
        downloadReportsPanel.setBackground(new Color(240, 240, 240));
        
        // Create controls panel for download tab
        JPanel downloadControls = new JPanel(new GridBagLayout());
        GridBagConstraints gbcDownload = new GridBagConstraints();
        gbcDownload.insets = new Insets(5, 5, 5, 5);
        gbcDownload.fill = GridBagConstraints.HORIZONTAL;
        
        // Add course selection
        JComboBox<String> downloadCourseCombo = new JComboBox<>();
        downloadCourseCombo.setPreferredSize(new Dimension(200, 30));
        
        // Add date fields
        JTextField downloadStartDate = new JTextField(10);
        JTextField downloadEndDate = new JTextField(10);
        
        // Add download button
        JButton downloadReportBtn = new JButton("Download Low Attendance Report");
        try {
            ImageIcon icon = new ImageIcon("images/download_icon.png");
            downloadReportBtn.setIcon(icon);
        } catch (Exception e) {
            // Continue without icon if not found
        }
        downloadReportBtn.setToolTipText("Download low attendance report as CSV file");
        
        // Add hover effect
        downloadReportBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                downloadReportBtn.setBackground(new Color(200, 230, 255));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                downloadReportBtn.setBackground(UIManager.getColor("Button.background"));
            }
        });
        
        // Layout controls
        gbcDownload.gridx = 0; gbcDownload.gridy = 0;
        downloadControls.add(new JLabel("Course:"), gbcDownload);
        gbcDownload.gridx = 1;
        downloadControls.add(downloadCourseCombo, gbcDownload);
        
        gbcDownload.gridx = 0; gbcDownload.gridy = 1;
        downloadControls.add(new JLabel("Start Date (YYYY-MM-DD):"), gbcDownload);
        gbcDownload.gridx = 1;
        downloadControls.add(downloadStartDate, gbcDownload);
        
        gbcDownload.gridx = 0; gbcDownload.gridy = 2;
        downloadControls.add(new JLabel("End Date (YYYY-MM-DD):"), gbcDownload);
        gbcDownload.gridx = 1;
        downloadControls.add(downloadEndDate, gbcDownload);
        
        gbcDownload.gridx = 0; gbcDownload.gridy = 3;
        gbcDownload.gridwidth = 2;
        downloadControls.add(downloadReportBtn, gbcDownload);
        
        // Add info label
        JLabel downloadInfoLabel = new JLabel("<html><div style='text-align: center;'>" +
            "Download low attendance reports for selected date range<br>" +
            "Reports will be saved as CSV files</div></html>");
        downloadInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbcDownload.gridy = 4;
        downloadControls.add(downloadInfoLabel, gbcDownload);
        
        downloadReportsPanel.add(downloadControls, BorderLayout.NORTH);
        
        // Add tabs with icons
        try {
            ImageIcon attendanceTabIcon = new ImageIcon("images/attendance_tab.png");
            ImageIcon reportTabIcon = new ImageIcon("images/report_tab.png");
            ImageIcon downloadTabIcon = new ImageIcon("images/download_tab.png");
            tabbedPane.addTab("Mark Attendance", attendanceTabIcon, markAttendancePanel);
            tabbedPane.addTab("View Reports", reportTabIcon, viewReportsPanel);
            tabbedPane.addTab("Download Reports", downloadTabIcon, downloadReportsPanel);
        } catch (Exception e) {
            // Add tabs without icons if images not found
            tabbedPane.addTab("Mark Attendance", markAttendancePanel);
            tabbedPane.addTab("View Reports", viewReportsPanel);
            tabbedPane.addTab("Download Reports", downloadReportsPanel);
        }
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
        
        // Load courses for all dropdowns
        loadCourses();
        loadCoursesForDownload(downloadCourseCombo);
        
        // Add action listeners
        courseCombo.addActionListener(e -> loadStudentsForCourse());

        markAttendanceBtn.addActionListener(e -> {
            Date selectedDate = (Date) dateSpinner.getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(selectedDate);
            saveAttendance(date);
        });

        editAttendanceBtn.addActionListener(e -> {
            Date selectedDate = (Date) dateSpinner.getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(selectedDate);
            loadAttendanceForDate(date);
        });

        generateReportBtn.addActionListener(e -> generateReport(reportCourseCombo, startDateField, endDateField, reportModel));
        lowAttendanceBtn.addActionListener(e -> generateLowAttendanceReport(reportModel));
        downloadReportBtn.addActionListener(e -> downloadLowAttendanceReport(
            downloadCourseCombo, downloadStartDate, downloadEndDate));
    }
    
    private void loadCourses() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id, name FROM courses WHERE teacher_id = ?");
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            courseCombo.removeAllItems();
            reportCourseCombo.removeAllItems();
            
            while (rs.next()) {
                String courseName = rs.getString("name");
                courseCombo.addItem(courseName);
                reportCourseCombo.addItem(courseName);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage());
        }
    }
    
    private void loadCoursesForDownload(JComboBox<String> comboBox) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT name FROM courses WHERE teacher_id = ?");
            pstmt.setInt(1, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            comboBox.removeAllItems();
            
            while (rs.next()) {
                comboBox.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading courses: " + ex.getMessage());
        }
    }
    
    private void loadStudentsForCourse() {
        String selectedCourse = (String) courseCombo.getSelectedItem();
        if (selectedCourse == null) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get course ID and batch
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id, batch FROM courses WHERE name = ? AND teacher_id = ?");
            pstmt.setString(1, selectedCourse);
            pstmt.setInt(2, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Course not found!");
                return;
            }
            
            int courseId = rs.getInt("id");
            String batch = rs.getString("batch");
            
            // Get all students in the batch
            pstmt = conn.prepareStatement(
                "SELECT roll_no, name FROM students WHERE batch = ?");
            pstmt.setString(1, batch);
            rs = pstmt.executeQuery();
            
            attendanceModel.setRowCount(0);
            while (rs.next()) {
                attendanceModel.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    Boolean.TRUE,  // Default to present
                    Boolean.FALSE  // Default to not late
                });
            }
            
            statusLabel.setText("Students loaded successfully");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading students: " + ex.getMessage());
            statusLabel.setText("Error loading students");
        }
    }
    
    private void saveAttendance(String date) {
        String selectedCourse = (String) courseCombo.getSelectedItem();
        if (selectedCourse == null) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get course ID
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM courses WHERE name = ? AND teacher_id = ?");
            pstmt.setString(1, selectedCourse);
            pstmt.setInt(2, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Course not found!");
                return;
            }
            int courseId = rs.getInt("id");
            
            // Check if attendance already exists for today
            pstmt = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM attendance " +
                "WHERE course_id = ? AND date = ?");
            pstmt.setInt(1, courseId);
            pstmt.setString(2, date);
            rs = pstmt.executeQuery();
            rs.next();
            int existingCount = rs.getInt("count");
            
            if (existingCount > 0) {
                int option = JOptionPane.showConfirmDialog(this,
                    "Attendance already exists for today. Do you want to edit it?",
                    "Attendance Exists",
                    JOptionPane.YES_NO_OPTION);
                
                if (option == JOptionPane.YES_OPTION) {
                    // Delete existing attendance
                    pstmt = conn.prepareStatement(
                        "DELETE FROM attendance WHERE course_id = ? AND date = ?");
                    pstmt.setInt(1, courseId);
                    pstmt.setString(2, date);
                    pstmt.executeUpdate();
                } else {
                    return;
                }
            }
            
            // Check for consecutive late attendance
            pstmt = conn.prepareStatement(
                "SELECT roll_no, status, date FROM attendance " +
                "WHERE course_id = ? AND date = DATE_SUB(?, INTERVAL 1 DAY)");
            pstmt.setInt(1, courseId);
            pstmt.setString(2, date);
            rs = pstmt.executeQuery();
            
            // Create a map of students who were late yesterday
            java.util.Map<String, Boolean> lateYesterday = new java.util.HashMap<>();
            while (rs.next()) {
                if (rs.getString("status").equals("Late")) {
                    lateYesterday.put(rs.getString("roll_no"), true);
                }
            }
            
            // Save attendance for each student
            pstmt = conn.prepareStatement(
                "INSERT INTO attendance (roll_no, course_id, date, status) VALUES (?, ?, ?, ?)");
            
            for (int i = 0; i < attendanceModel.getRowCount(); i++) {
                String rollNo = (String) attendanceModel.getValueAt(i, 0);
                boolean present = (Boolean) attendanceModel.getValueAt(i, 2);
                boolean late = (Boolean) attendanceModel.getValueAt(i, 3);
                
                String status;
                if (late && lateYesterday.containsKey(rollNo)) {
                    // If student was late yesterday and is late today, mark as absent
                    status = "Absent";
                } else if (late) {
                    status = "Late";
                } else if (present) {
                    status = "Present";
                } else {
                    status = "Absent";
                }
                
                pstmt.setString(1, rollNo);
                pstmt.setInt(2, courseId);
                pstmt.setString(3, date);
                pstmt.setString(4, status);
                pstmt.executeUpdate();
            }
            
            statusLabel.setText("Attendance saved successfully!");
            JOptionPane.showMessageDialog(this, "Attendance saved successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + ex.getMessage());
            statusLabel.setText("Error saving attendance");
        }
    }
    
    private void generateReport(JComboBox<String> courseCombo, JTextField startDate, JTextField endDate, DefaultTableModel reportModel) {
        String selectedCourse = (String) courseCombo.getSelectedItem();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course");
            return;
        }
        
        if (startDate.getText().isEmpty() || endDate.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both start and end dates");
            return;
        }
        
        // Validate date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(startDate.getText());
            sdf.parse(endDate.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter dates in YYYY-MM-DD format");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First get the course ID
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM courses WHERE name = ? AND teacher_id = ?");
            pstmt.setString(1, selectedCourse);
            pstmt.setInt(2, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Course not found!");
                return;
            }
            
            int courseId = rs.getInt("id");
            
            // Now get the attendance records with percentage
            pstmt = conn.prepareStatement(
                "SELECT s.roll_no, s.name, a.date, a.status, " +
                "calculate_attendance_percentage(s.roll_no, a.course_id, ?, ?) as percentage " +
                "FROM attendance a " +
                "JOIN students s ON a.roll_no = s.roll_no " +
                "WHERE a.course_id = ? AND a.date BETWEEN ? AND ? " +
                "ORDER BY a.date DESC, s.roll_no");
            
            pstmt.setString(1, startDate.getText());
            pstmt.setString(2, endDate.getText());
            pstmt.setInt(3, courseId);
            pstmt.setString(4, startDate.getText());
            pstmt.setString(5, endDate.getText());
            
            rs = pstmt.executeQuery();
            reportModel.setRowCount(0);
            
            boolean hasRecords = false;
            double totalPercentage = 0;
            int recordCount = 0;
            
            while (rs.next()) {
                hasRecords = true;
                double percentage = rs.getDouble("percentage");
                totalPercentage += percentage;
                recordCount++;
                
                reportModel.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    rs.getString("date"),
                    rs.getString("status"),
                    String.format("%.2f%%", percentage)
                });
            }
            
            if (!hasRecords) {
                JOptionPane.showMessageDialog(this, "No attendance records found for the selected date range");
            } else {
                // Calculate and update average attendance
                double averagePercentage = totalPercentage / recordCount;
                attendanceProgressBar.setValue((int) averagePercentage);
                attendanceProgressBar.setForeground(averagePercentage < 75 ? Color.RED : Color.GREEN);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void generateLowAttendanceReport(DefaultTableModel reportModel) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students_low_attendance");
            
            reportModel.setRowCount(0);
            boolean hasRecords = false;
            double totalPercentage = 0;
            int recordCount = 0;
            
            while (rs.next()) {
                hasRecords = true;
                double percentage = rs.getDouble("attendance_percentage");
                totalPercentage += percentage;
                recordCount++;
                
                reportModel.addRow(new Object[]{
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    rs.getString("course_name"),
                    "Low Attendance",
                    String.format("%.2f%%", percentage),
                    rs.getInt("present_days"),
                    rs.getInt("late_days"),
                    rs.getInt("absent_days"),
                    rs.getInt("total_days")
                });
            }
            
            if (!hasRecords) {
                JOptionPane.showMessageDialog(this, "No students found with low attendance");
            } else {
                // Calculate and update average attendance
                double averagePercentage = totalPercentage / recordCount;
                attendanceProgressBar.setValue((int) averagePercentage);
                attendanceProgressBar.setForeground(Color.RED);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating low attendance report: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadExistingAttendance() {
        String selectedCourse = (String) courseCombo.getSelectedItem();
        if (selectedCourse == null) return;
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get course ID
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM courses WHERE name = ? AND teacher_id = ?");
            pstmt.setString(1, selectedCourse);
            pstmt.setInt(2, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Course not found!");
                return;
            }
            int courseId = rs.getInt("id");
            
            // Get existing attendance for today
            pstmt = conn.prepareStatement(
                "SELECT a.roll_no, a.status, s.name " +
                "FROM attendance a " +
                "JOIN students s ON a.roll_no = s.roll_no " +
                "WHERE a.course_id = ? AND a.date = ?");
            pstmt.setInt(1, courseId);
            pstmt.setString(2, currentDate);
            rs = pstmt.executeQuery();
            
            // Clear existing table
            attendanceModel.setRowCount(0);
            
            // Load students and their attendance status
            while (rs.next()) {
                String rollNo = rs.getString("roll_no");
                String name = rs.getString("name");
                String status = rs.getString("status");
                
                boolean present = status.equals("Present");
                boolean late = status.equals("Late");
                
                attendanceModel.addRow(new Object[]{rollNo, name, present, late});
            }
            
            statusLabel.setText("Loaded existing attendance for editing");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading attendance: " + ex.getMessage());
            statusLabel.setText("Error loading attendance");
        }
    }

    private void loadAttendanceForDate(String date) {
        String selectedCourse = (String) courseCombo.getSelectedItem();
        if (selectedCourse == null) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get course ID
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM courses WHERE name = ? AND teacher_id = ?");
            pstmt.setString(1, selectedCourse);
            pstmt.setInt(2, teacherId);
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Course not found!");
                return;
            }
            int courseId = rs.getInt("id");
            
            // Get existing attendance for the date
            pstmt = conn.prepareStatement(
                "SELECT a.roll_no, a.status, s.name " +
                "FROM attendance a " +
                "JOIN students s ON a.roll_no = s.roll_no " +
                "WHERE a.course_id = ? AND a.date = ?");
            pstmt.setInt(1, courseId);
            pstmt.setString(2, date);
            rs = pstmt.executeQuery();
            
            // Clear existing table
            attendanceModel.setRowCount(0);
            
            // Load students and their attendance status
            while (rs.next()) {
                String rollNo = rs.getString("roll_no");
                String name = rs.getString("name");
                String status = rs.getString("status");
                
                boolean present = status.equals("Present");
                boolean late = status.equals("Late");
                
                attendanceModel.addRow(new Object[]{rollNo, name, present, late});
            }
            
            statusLabel.setText("Loaded attendance for " + date);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading attendance: " + ex.getMessage());
            statusLabel.setText("Error loading attendance");
        }
    }

    private void downloadLowAttendanceReport(JComboBox<String> courseCombo, JTextField startDate, JTextField endDate) {
        String selectedCourse = (String) courseCombo.getSelectedItem();
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course");
            return;
        }
        
        if (startDate.getText().isEmpty() || endDate.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both start and end dates");
            return;
        }
        
        // Validate date format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(startDate.getText());
            sdf.parse(endDate.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter dates in YYYY-MM-DD format");
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get course ID
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM courses WHERE name = ? AND teacher_id = ?");
            pstmt.setString(1, selectedCourse);
            pstmt.setInt(2, teacherId);
            ResultSet rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Course not found!");
                return;
            }
            
            int courseId = rs.getInt("id");
            
            // Get low attendance data
            pstmt = conn.prepareStatement(
                "SELECT s.roll_no, s.name, " +
                "COUNT(CASE WHEN a.status = 'Present' THEN 1 END) as present_days, " +
                "COUNT(CASE WHEN a.status = 'Late' THEN 1 END) as late_days, " +
                "COUNT(CASE WHEN a.status = 'Absent' THEN 1 END) as absent_days, " +
                "COUNT(*) as total_days, " +
                "calculate_attendance_percentage(s.roll_no, ?, ?, ?) as percentage " +
                "FROM students s " +
                "JOIN attendance a ON s.roll_no = a.roll_no " +
                "WHERE a.course_id = ? AND a.date BETWEEN ? AND ? " +
                "GROUP BY s.roll_no, s.name " +
                "HAVING percentage < 75 " +
                "ORDER BY percentage ASC");
            
            pstmt.setInt(1, courseId);
            pstmt.setString(2, startDate.getText());
            pstmt.setString(3, endDate.getText());
            pstmt.setInt(4, courseId);
            pstmt.setString(5, startDate.getText());
            pstmt.setString(6, endDate.getText());
            
            rs = pstmt.executeQuery();
            
            // Create file chooser
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Low Attendance Report");
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            
            // Set default filename
            String defaultFilename = "Low_Attendance_Report_" + selectedCourse + "_" + 
                                   startDate.getText() + "_to_" + endDate.getText() + ".csv";
            fileChooser.setSelectedFile(new File(defaultFilename));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getPath() + ".csv");
                }
                
                try (FileWriter writer = new FileWriter(file)) {
                    // Write header
                    writer.write("Roll No,Name,Present Days,Late Days,Absent Days,Total Days,Attendance %\n");
                    
                    // Write data rows
                    while (rs.next()) {
                        writer.write(String.format("%s,%s,%d,%d,%d,%d,%.2f%%\n",
                            rs.getString("roll_no"),
                            rs.getString("name"),
                            rs.getInt("present_days"),
                            rs.getInt("late_days"),
                            rs.getInt("absent_days"),
                            rs.getInt("total_days"),
                            rs.getDouble("percentage")));
                    }
                    
                    JOptionPane.showMessageDialog(this, "Report downloaded successfully!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage());
        }
    }
} 