package DAO;

import Model.StudentResult;
import java.util.List;

public interface StudentResultDAO {
    List<StudentResult> getResultsByStudentId(String studentId);
}