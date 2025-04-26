# Attendance Management System

A Java Swing application for managing student attendance with separate interfaces for administrators and teachers.

## Features

- Admin Dashboard
  - Manage teachers (add, edit, delete)
  - Manage students (add, edit, delete)
  - Manage courses (add, edit, delete)
  - Assign courses to teachers

- Teacher Dashboard
  - Mark attendance for courses
  - View attendance reports
  - Filter reports by date range

## Prerequisites

- Java JDK 8 or higher
- MySQL Server
- MySQL Connector/J (JDBC driver)

## Setup Instructions

1. Install MySQL Server on your system
2. Create a new database using the provided schema.sql file:
   ```bash
   mysql -u root -p < schema.sql
   ```
   (Use password: Mano@2005 when prompted)

3. Download and add the MySQL Connector/J to your project's classpath:
   - Download from: https://dev.mysql.com/downloads/connector/j/
   - Add the JAR file to your project's build path

## Running the Application

1. Compile all Java files:
   ```bash
   javac *.java
   ```

2. Run the application:
   ```bash
   java LoginScreen
   ```

## Default Login Credentials

- Admin:
  - Username: admin
  - Password: admin123

- Teacher:
  - Username: teacher1
  - Password: teacher123

## Database Configuration

The application uses the following database configuration:
- Database Name: javaDemoPrj
- Username: root
- Password: Mano@2005

To change these settings, modify the `DatabaseConnection.java` file.

## Project Structure

- `LoginScreen.java`: Main login interface
- `AdminDashboard.java`: Admin management interface
- `TeacherDashboard.java`: Teacher attendance interface
- `DatabaseConnection.java`: Database connection utility
- `schema.sql`: Database schema and initial data

## License

This project is licensed under the MIT License. 