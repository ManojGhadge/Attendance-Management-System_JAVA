-- Create database
CREATE DATABASE IF NOT EXISTS javaDemoPrj;
USE javaDemoPrj;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role ENUM('admin', 'teacher') NOT NULL,
    teacher_id INT
);

-- Create teachers table
CREATE TABLE IF NOT EXISTS teachers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
);

-- Create courses table
CREATE TABLE IF NOT EXISTS courses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    teacher_id INT,
    batch VARCHAR(50),
    FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

-- Create students table
CREATE TABLE IF NOT EXISTS students (
    roll_no VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    batch VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL
);

-- Create attendance table with updated status column
CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    roll_no VARCHAR(20),
    course_id INT,
    date DATE NOT NULL,
    status ENUM('Present', 'Absent', 'Late') NOT NULL,
    FOREIGN KEY (roll_no) REFERENCES students(roll_no),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);

-- Create function to calculate attendance percentage
DELIMITER //
CREATE FUNCTION calculate_attendance_percentage(
    p_roll_no VARCHAR(20),
    p_course_id INT,
    p_start_date DATE,
    p_end_date DATE
) RETURNS DECIMAL(5,2)
DETERMINISTIC
BEGIN
    DECLARE total_classes INT;
    DECLARE present_classes INT;
    
    -- Count total classes in the date range
    SELECT COUNT(*) INTO total_classes
    FROM attendance
    WHERE course_id = p_course_id
    AND date BETWEEN p_start_date AND p_end_date;
    
    -- Count present classes (including late)
    SELECT COUNT(*) INTO present_classes
    FROM attendance
    WHERE roll_no = p_roll_no
    AND course_id = p_course_id
    AND date BETWEEN p_start_date AND p_end_date
    AND status IN ('Present', 'Late');
    
    -- Calculate percentage
    IF total_classes = 0 THEN
        RETURN 0;
    ELSE
        RETURN (present_classes * 100.0) / total_classes;
    END IF;
END //
DELIMITER ;

-- Create view for low attendance
CREATE VIEW students_low_attendance AS
SELECT 
    s.roll_no,
    s.name,
    c.name AS course_name,
    calculate_attendance_percentage(s.roll_no, c.id, 
        DATE_SUB(CURDATE(), INTERVAL 30 DAY), 
        CURDATE()) AS attendance_percentage
FROM students s
JOIN courses c ON s.batch = c.batch
WHERE calculate_attendance_percentage(s.roll_no, c.id, 
    DATE_SUB(CURDATE(), INTERVAL 30 DAY), 
    CURDATE()) < 75;





   -- Insert teachers
INSERT INTO teachers (name, email) VALUES 
('Sharmila Kharhat', 'teacher1@egmail.com'),
('Minakshi Vharkahte', 'teacher2@gmail.com');

-- Insert users
INSERT INTO users (username, password, role, teacher_id) VALUES 
('admin', 'admin123', 'admin', NULL),
('teacher1', 'teacher123', 'teacher', 1),
('teacher2', 'teacher123', 'teacher', 2);

-- Insert courses
INSERT INTO courses (name, teacher_id, batch) VALUES 
('Core Java', 1, '2025'),
('DBMS', 2, '2025');

-- Insert students
INSERT INTO students (roll_no, name, batch, email) VALUES 
('38', 'Dnyaneshwar Kote', '2025', 'kote@gmail.com'),
('42', 'Manoj Ghadge', '2025', 'manoj@gmail.com'),
('44', 'Prathamesh Kusram', '2025', 'Prathamesh@gmail.com');


select * from users;