package DAO.Impl;

import DAO.ResultDAO;
import Database.DatabaseConnection;
import Model.Result;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResultDAOImpl implements ResultDAO {

    @Override
    public List<Result> getAllResults() {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT r.*, c.course_name, c.credits " +
                "FROM results r " +
                "JOIN enrollments e ON r.enrollment_id = e.enrollment_id " +
                "JOIN courses c ON e.course_id = c.course_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Result result = mapResult(rs);
                results.add(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public Result getResultByEnrollmentId(int enrollmentId) {
        String sql = "SELECT r.*, c.course_name, c.credits " +
                "FROM results r " +
                "JOIN enrollments e ON r.enrollment_id = e.enrollment_id " +
                "JOIN courses c ON e.course_id = c.course_id " +
                "WHERE r.enrollment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResult(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Result> getResultsByStudentId(int studentId) {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT r.*, c.course_name, c.credits " +
                "FROM results r " +
                "JOIN enrollments e ON r.enrollment_id = e.enrollment_id " +
                "JOIN courses c ON e.course_id = c.course_id " +
                "WHERE e.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Result result = mapResult(rs);
                    results.add(result);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public List<Result> getResultsByCourseId(int courseId) {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT r.*, c.course_name, c.credits " +
                "FROM results r " +
                "JOIN enrollments e ON r.enrollment_id = e.enrollment_id " +
                "JOIN courses c ON e.course_id = c.course_id " +
                "WHERE e.course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Result result = mapResult(rs);
                    results.add(result);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    private Result mapResult(ResultSet rs) throws SQLException {
        Result result = new Result();
        result.setResultId(rs.getInt("result_id"));
        result.setEnrollmentId(rs.getInt("enrollment_id"));
        result.setGrade(rs.getString("grade"));
        result.setGpa(rs.getDouble("gpa_points"));
        result.setDegree(rs.getInt("degree"));
        Date date = rs.getDate("exam_date");
        result.setExamDate(date != null ? date.toLocalDate() : null);
        result.setCourseName(rs.getString("course_name"));
        result.setCredits(rs.getInt("credits"));
        return result;
    }

    @Override
    public boolean addResult(Result result) {
        String sql = "INSERT INTO results (enrollment_id, grade, gpa_points, degree, exam_date) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, result.getEnrollmentId());
            pstmt.setString(2, result.getGrade());
            pstmt.setDouble(3, result.getGpa());
            pstmt.setInt(4, result.getDegree());
            pstmt.setDate(5, result.getExamDate() != null ? Date.valueOf(result.getExamDate()) : null);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateResult(Result result) {
        String sql = "UPDATE results SET grade = ?, gpa_points = ?, degree = ?, exam_date = ? " +
                "WHERE result_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, result.getGrade());
            pstmt.setDouble(2, result.getGpa());
            pstmt.setInt(3, result.getDegree());
            pstmt.setDate(4, result.getExamDate() != null ? Date.valueOf(result.getExamDate()) : null);
            pstmt.setInt(5, result.getResultId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteResult(int resultId) {
        String sql = "DELETE FROM results WHERE result_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, resultId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}