package DAO.Impl;

import DAO.StudentResultDAO;
import Model.StudentResult;
import Database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentResultDAOImpl implements StudentResultDAO {

    @Override
    public List<StudentResult> getResultsByStudentId(String studentId) {
        List<StudentResult> results = new ArrayList<>();
        String sql = "SELECT r.*, c.course_name FROM student_results r " +
                "JOIN courses c ON r.course_id = c.course_id " +
                "WHERE r.student_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                StudentResult result = new StudentResult();
                result.setResultId(rs.getInt("result_id"));
                result.setStudentId(rs.getString("student_id"));
                result.setCourseId(rs.getInt("course_id"));
                result.setCourseName(rs.getString("course_name"));
                result.setDegree(rs.getDouble("degree"));
                result.setGrade(rs.getString("grade"));
                results.add(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}