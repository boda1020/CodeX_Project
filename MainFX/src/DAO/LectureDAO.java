package DAO;

import Model.Lecture;
import java.util.List;

public interface LectureDAO {
    List<Lecture> getLecturesByCourseId(int courseId);
}