package Model;

public class StudentResult {
    private int resultId;
    private String studentId;
    private int courseId;
    private String courseName;
    private double degree;
    private String grade;

    public StudentResult() {}

    // Getters and Setters
    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public double getDegree() { return degree; }
    public void setDegree(double degree) { this.degree = degree; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}