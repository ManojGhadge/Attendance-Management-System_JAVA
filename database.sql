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
    DECLARE present_count INT;
    DECLARE late_count INT;
    DECLARE percentage DECIMAL(5,2);


    
    -- Count total classes in the date range
    SELECT COUNT(DISTINCT date) INTO total_classes
    FROM attendance
    WHERE course_id = p_course_id
    AND date BETWEEN p_start_date AND p_end_date;
    
    -- Count present and late classes
    SELECT 
        COUNT(CASE WHEN status = 'Present' THEN 1 END),
        COUNT(CASE WHEN status = 'Late' THEN 1 END)
    INTO present_count, late_count
    FROM attendance
    WHERE roll_no = p_roll_no
    AND course_id = p_course_id
    AND date BETWEEN p_start_date AND p_end_date;
    
    -- Calculate percentage (Late counts as 0.5 attendance)
    IF total_classes > 0 THEN
        SET percentage = ((present_count + (late_count * 0.5)) / total_classes) * 100;
    ELSE
        SET percentage = 0;
    END IF;
    
    RETURN percentage;
END //

DELIMITER ; 