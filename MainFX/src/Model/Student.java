package Model;

public class Student {
    private int studentId;
    private String studentCode;
    private String level;
    private String department;
    private double gpa;
    private String status;

    // الحقول اللي جاية من users عشان الواجهة
    private String fullName;
    private String email;
    private String phone;

    public Student() {}

    // Getters & Setters الأصلية
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // الحقول الجديدة
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}