package DAO;

import Model.Result;
import java.util.List;

public interface ResultDAO {

    // Get all results
    List<Result> getAllResults();

    // Get results for a specific enrollment
    Result getResultByEnrollmentId(int enrollmentId);

    // Get results for a specific student
    List<Result> getResultsByStudentId(int studentId);

    // Get results for a specific course
    List<Result> getResultsByCourseId(int courseId);

    // Add new result
    boolean addResult(Result result);

    // Update existing result
    boolean updateResult(Result result);

    // Delete result by enrollment ID
    boolean deleteResult(int enrollmentId);
}