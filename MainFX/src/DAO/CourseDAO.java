package DAO;

import Model.Course;
import java.util.List;

public interface CourseDAO {

    List<Course> getAllCourses();

    Course getCourseById(int courseId);

    Course getCourseByCode(String courseCode);

    Course getCourseByName(String courseName);  // جديد

    boolean addCourse(Course course);

    boolean updateCourse(Course course);

    boolean deleteCourse(int courseId);

    List<Course> searchCourses(String query);

    List<Course> getCoursesByLevel(String level);
    List<String> getAllDepartments();

    int getTotalCourses();
}