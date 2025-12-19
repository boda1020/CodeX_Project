package DAO;

import Model.Student;
import java.util.List;

public interface StudentDAO {

    // Get all students
    List<Student> getAllStudents();

    // Get student by ID
    Student getStudentById(int studentId);

    // Get student by student code
    Student getStudentByCode(String studentCode);

    // Add new student
    boolean addStudent(Student student);

    // Update existing student
    boolean updateStudent(Student student);

    // Delete student by ID
    boolean deleteStudent(int studentId);

    // Search students by name, code, or email (from users table)
    List<Student> searchStudents(String query);

    List<Student> getStudentsByLevel(String level);

    List<Student> getStudentsByDepartment(String department);

    List<Student> getTopPerformingStudents(int limit);

    // Get total number of students
    int getTotalStudents();

    // Get average GPA
    double getAverageGPA();

    // NEW METHOD: Update student GPA
    boolean updateStudentGPA(int studentId, double gpa);

}