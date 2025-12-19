package DAO.Impl;

import DAO.LectureDAO;
import Model.Lecture;
import Database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LectureDAOImpl implements LectureDAO {

    @Override
    public List<Lecture> getLecturesByCourseId(int courseId) {
        List<Lecture> lectures = new ArrayList<>();
        String sql = "SELECT * FROM lectures WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Lecture lecture = new Lecture();
                lecture.setLectureId(rs.getInt("lecture_id"));
                lecture.setCourseId(rs.getInt("course_id"));
                lecture.setTitle(rs.getString("title"));
                lecture.setFilePath(rs.getString("file_path"));
                lectures.add(lecture);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lectures;
    }
}