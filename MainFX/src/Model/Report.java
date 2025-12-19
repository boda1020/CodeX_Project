package Model;

import java.time.LocalDate;

public class Report {
    private int reportId;
    private String reportType;
    private String title;
    private String description;
    private String content;
    private String generatedBy;
    private LocalDate generationDate;
    private String level;
    private String department;
    private int downloadCount;

    // Constructors
    public Report() {}

    public Report(String reportType, String title, String description, String content,
                  String generatedBy, LocalDate generationDate, String level, String department) {
        this.reportType = reportType;
        this.title = title;
        this.description = description;
        this.content = content;
        this.generatedBy = generatedBy;
        this.generationDate = generationDate;
        this.level = level;
        this.department = department;
        this.downloadCount = 0;
    }

    // Getters and Setters
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public LocalDate getGenerationDate() { return generationDate; }
    public void setGenerationDate(LocalDate generationDate) { this.generationDate = generationDate; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }

    @Override
    public String toString() {
        return title + " (" + reportType + ") - " + generationDate;
    }
}