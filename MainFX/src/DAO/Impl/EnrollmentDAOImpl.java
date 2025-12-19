package DAO.Impl;

import DAO.EnrollmentDAO;
import Model.Enrollment;
import Database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAOImpl implements EnrollmentDAO {

    @Override
    public List<Enrollment> getAllEnrollments() {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollment.setStudentId(rs.getInt("user_id"));  // تعديل: user_id
                enrollment.setCourseId(rs.getInt("course_id"));
                Date date = rs.getDate("enrollment_date");
                enrollment.setEnrollmentDate(date != null ? date.toLocalDate() : null);
                enrollment.setProgressPercentage(rs.getDouble("progress_percentage"));
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    @Override
    public List<Enrollment> getEnrollmentsByStudentId(int userId) {  // اسم المتغير للتوضيح
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE user_id = ?";  // تعديل: user_id

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                    enrollment.setStudentId(rs.getInt("user_id"));  // تعديل: user_id
                    enrollment.setCourseId(rs.getInt("course_id"));
                    Date date = rs.getDate("enrollment_date");
                    enrollment.setEnrollmentDate(date != null ? date.toLocalDate() : null);
                    enrollment.setProgressPercentage(rs.getDouble("progress_percentage"));
                    enrollments.add(enrollment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    @Override
    public List<Enrollment> getEnrollmentsByCourseId(int courseId) {
        List<Enrollment> enrollments = new ArrayList<>();
        String sql = "SELECT * FROM enrollments WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                    enrollment.setStudentId(rs.getInt("user_id"));  // تعديل: user_id
                    enrollment.setCourseId(rs.getInt("course_id"));
                    Date date = rs.getDate("enrollment_date");
                    enrollment.setEnrollmentDate(date != null ? date.toLocalDate() : null);
                    enrollment.setProgressPercentage(rs.getDouble("progress_percentage"));
                    enrollments.add(enrollment);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enrollments;
    }

    @Override
    public boolean enrollStudent(int userId, int courseId) {
        if (isEnrolled(userId, courseId)) {
            return false;
        }

        String sql = "INSERT INTO enrollments (user_id, course_id) VALUES (?, ?)";  // تعديل: user_id

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, courseId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateProgress(int enrollmentId, double progressPercentage) {
        String sql = "UPDATE enrollments SET progress_percentage = ? WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, progressPercentage);
            pstmt.setInt(2, enrollmentId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean unenrollStudent(int userId, int courseId) {
        String sql = "DELETE FROM enrollments WHERE user_id = ? AND course_id = ?";  // تعديل: user_id

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, courseId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isEnrolled(int userId, int courseId) {
        String sql = "SELECT 1 FROM enrollments WHERE user_id = ? AND course_id = ?";  // تعديل: user_id

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, courseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getTotalEnrollments() {
        String sql = "SELECT COUNT(*) FROM enrollments";

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
    public int getEnrollmentId(int userId, int courseId) {
        String sql = "SELECT enrollment_id FROM enrollments WHERE user_id = ? AND course_id = ?";  // تعديل: user_id

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("enrollment_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int addEnrollment(int userId, int courseId) {
        if (isEnrolled(userId, courseId)) {
            return getEnrollmentId(userId, courseId);
        }

        String sql = "INSERT INTO enrollments (user_id, course_id) VALUES (?, ?)";  // تعديل: user_id

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, courseId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}