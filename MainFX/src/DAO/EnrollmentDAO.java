package DAO;

import Model.Enrollment;
import java.util.List;

public interface EnrollmentDAO {

    List<Enrollment> getAllEnrollments();

    List<Enrollment> getEnrollmentsByStudentId(int studentId);

    List<Enrollment> getEnrollmentsByCourseId(int courseId);

    boolean enrollStudent(int studentId, int courseId);

    boolean updateProgress(int enrollmentId, double progressPercentage);

    boolean unenrollStudent(int studentId, int courseId);

    boolean isEnrolled(int studentId, int courseId);

    int getTotalEnrollments();

    // الدوال الجديدة اللي محتاجينها
    int getEnrollmentId(int studentId, int courseId);

    int addEnrollment(int studentId, int courseId); // يرجع الـ enrollment_id الجديد
}