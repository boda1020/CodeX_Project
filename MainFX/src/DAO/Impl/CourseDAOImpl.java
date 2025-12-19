package DAO.Impl;

import DAO.CourseDAO;
import Model.Course;
import Database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAOImpl implements CourseDAO {

    @Override
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Course course = mapCourse(rs);
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    @Override
    public Course getCourseById(int courseId) {
        String sql = "SELECT * FROM courses WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapCourse(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Course getCourseByCode(String courseCode) {
        String sql = "SELECT * FROM courses WHERE course_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapCourse(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Course getCourseByName(String courseName) {
        String sql = "SELECT * FROM courses WHERE course_name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, courseName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapCourse(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Course mapCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setCourseCode(rs.getString("course_code"));
        course.setCourseName(rs.getString("course_name"));
        course.setCredits(rs.getInt("credits"));
        course.setLevel(rs.getString("level"));
        course.setDepartment(rs.getString("department"));
        course.setInstructorName(rs.getString("instructor_name"));
        course.setRoom(rs.getString("room"));
        course.setSchedule(rs.getString("schedule"));
        course.setStatus(rs.getString("status"));
        course.setPdfPath(rs.getString("pdf_path"));
        course.setFolderName(rs.getString("folder_name"));
        course.setDescription(rs.getString("description"));
        return course;
    }

    @Override
    public boolean addCourse(Course course) {
        String sql = "INSERT INTO courses (course_code, course_name, credits, level, department, instructor_name, room, schedule, status, pdf_path, folder_name, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseName());
            pstmt.setInt(3, course.getCredits());
            pstmt.setString(4, course.getLevel());
            pstmt.setString(5, course.getDepartment());
            pstmt.setString(6, course.getInstructorName());
            pstmt.setString(7, course.getRoom());
            pstmt.setString(8, course.getSchedule());
            pstmt.setString(9, course.getStatus());
            pstmt.setString(10, course.getPdfPath());
            pstmt.setString(11, course.getFolderName());
            pstmt.setString(12, course.getDescription());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateCourse(Course course) {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, credits = ?, department = ?, instructor_name = ?, room = ?, schedule = ?, status = ?, pdf_path = ? " +
                "WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCourseCode());
            pstmt.setString(2, course.getCourseName());
            pstmt.setInt(3, course.getCredits());
            pstmt.setString(4, course.getDepartment());
            pstmt.setString(5, course.getInstructorName());
            pstmt.setString(6, course.getRoom());
            pstmt.setString(7, course.getSchedule());
            pstmt.setString(8, course.getStatus());
            pstmt.setString(9, course.getPdfPath());
            pstmt.setInt(10, course.getCourseId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteCourse(int courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Course> searchCourses(String query) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses WHERE course_code LIKE ? OR course_name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + query + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapCourse(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    @Override
    public List<Course> getCoursesByLevel(String level) {
        List<Course> courses = new ArrayList<>();
        if (level == null || level.trim().isEmpty()) {
            return courses;
        }

        String sql = "SELECT * FROM courses WHERE TRIM(UPPER(level)) = TRIM(UPPER(?))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, level);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getInt("course_id"));
                course.setCourseCode(rs.getString("course_code"));
                course.setCourseName(rs.getString("course_name"));
                course.setCredits(rs.getInt("credits"));
                course.setLevel(rs.getString("level"));
                course.setDepartment(rs.getString("department"));
                course.setInstructorName(rs.getString("instructor_name"));
                course.setRoom(rs.getString("room"));
                course.setSchedule(rs.getString("schedule"));
                course.setStatus(rs.getString("status"));
                course.setPdfPath(rs.getString("pdf_path"));
                course.setFolderName(rs.getString("folder_name"));
                course.setDescription(rs.getString("description"));
                courses.add(course);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    @Override
    public List<String> getAllDepartments() {
        return List.of();
    }

    @Override
    public int getTotalCourses() {
        String sql = "SELECT COUNT(*) FROM courses";
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
}