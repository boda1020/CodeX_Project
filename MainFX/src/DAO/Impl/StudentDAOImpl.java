package DAO.Impl;

import DAO.StudentDAO;
import Database.DatabaseConnection;
import Model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAOImpl implements StudentDAO {

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getInt("student_id"));
        student.setStudentCode(rs.getString("student_code"));
        student.setLevel(rs.getString("level"));
        student.setDepartment(rs.getString("department"));
        student.setGpa(rs.getDouble("gpa"));
        student.setStatus(rs.getString("status"));

        // الحقول من users
        student.setFullName(rs.getString("full_name"));
        student.setEmail(rs.getString("email"));
        student.setPhone(rs.getString("phone"));

        return student;
    }

    @Override
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name, u.email, u.phone " +
                "FROM students s JOIN users u ON s.student_id = u.user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    @Override
    public Student getStudentById(int studentId) {
        String sql = "SELECT s.*, u.full_name, u.email, u.phone " +
                "FROM students s JOIN users u ON s.student_id = u.user_id WHERE s.student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Student getStudentByCode(String studentCode) {
        String sql = "SELECT s.*, u.full_name, u.email, u.phone " +
                "FROM students s JOIN users u ON s.student_id = u.user_id WHERE s.student_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (student_id, student_code, level, department, gpa, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, student.getStudentId());
            pstmt.setString(2, student.getStudentCode());
            pstmt.setString(3, student.getLevel());
            pstmt.setString(4, student.getDepartment());
            pstmt.setDouble(5, student.getGpa());
            pstmt.setString(6, student.getStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET student_code = ?, level = ?, department = ?, gpa = ?, status = ? " +
                "WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getStudentCode());
            pstmt.setString(2, student.getLevel());
            pstmt.setString(3, student.getDepartment());
            pstmt.setDouble(4, student.getGpa());
            pstmt.setString(5, student.getStatus());
            pstmt.setInt(6, student.getStudentId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Student> searchStudents(String query) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name, u.email, u.phone " +
                "FROM students s JOIN users u ON s.student_id = u.user_id " +
                "WHERE u.full_name LIKE ? OR s.student_code LIKE ? OR u.email LIKE ? OR u.phone LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + query + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            pstmt.setString(3, searchTerm);
            pstmt.setString(4, searchTerm);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    @Override
    public List<Student> getStudentsByLevel(String level) {
        return List.of();
    }

    @Override
    public List<Student> getStudentsByDepartment(String department) {
        return List.of();
    }

    @Override
    public List<Student> getTopPerformingStudents(int limit) {
        return List.of();
    }

    @Override
    public int getTotalStudents() {
        String sql = "SELECT COUNT(*) FROM students";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public double getAverageGPA() {
        String sql = "SELECT AVG(gpa) FROM students WHERE gpa IS NOT NULL AND gpa > 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                double avg = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : avg;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public boolean updateStudentGPA(int studentId, double gpa) {
        String sql = "UPDATE students SET gpa = ? WHERE student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, gpa);
            pstmt.setInt(2, studentId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}