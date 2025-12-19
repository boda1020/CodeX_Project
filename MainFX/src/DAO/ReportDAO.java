package DAO;

import Database.DatabaseConnection;
import Model.Report;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // Get all reports
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM reports ORDER BY generation_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }

    // Get report by ID
    public Report getReportById(int reportId) {
        String sql = "SELECT * FROM reports WHERE report_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reportId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToReport(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Generate new report
    public boolean generateReport(Report report) {
        String sql = """
            INSERT INTO reports 
            (report_type, title, description, content, generated_by, 
             generation_date, level, department) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, report.getReportType());
            pstmt.setString(2, report.getTitle());
            pstmt.setString(3, report.getDescription());
            pstmt.setString(4, report.getContent());
            pstmt.setString(5, report.getGeneratedBy());
            pstmt.setDate(6, Date.valueOf(report.getGenerationDate()));
            pstmt.setString(7, report.getLevel());
            pstmt.setString(8, report.getDepartment());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get filtered reports
    public List<Report> getFilteredReports(String reportType, String level,
                                           String department, LocalDate startDate,
                                           LocalDate endDate, String searchText) {
        List<Report> reports = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM reports WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (reportType != null && !reportType.equals("All Reports")) {
            sql.append(" AND report_type = ?");
            params.add(reportType);
        }

        if (level != null && !level.equals("All Levels")) {
            sql.append(" AND level = ?");
            params.add(level);
        }

        if (department != null && !department.equals("All Departments")) {
            sql.append(" AND department = ?");
            params.add(department);
        }

        if (startDate != null) {
            sql.append(" AND generation_date >= ?");
            params.add(Date.valueOf(startDate));
        }

        if (endDate != null) {
            sql.append(" AND generation_date <= ?");
            params.add(Date.valueOf(endDate));
        }

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            String searchPattern = "%" + searchText + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        sql.append(" ORDER BY generation_date DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reports.add(mapResultSetToReport(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }

    // Update report
    public boolean updateReport(Report report) {
        String sql = """
            UPDATE reports SET 
            report_type = ?, title = ?, description = ?, content = ?, 
            level = ?, department = ?, download_count = ?
            WHERE report_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, report.getReportType());
            pstmt.setString(2, report.getTitle());
            pstmt.setString(3, report.getDescription());
            pstmt.setString(4, report.getContent());
            pstmt.setString(5, report.getLevel());
            pstmt.setString(6, report.getDepartment());
            pstmt.setInt(7, report.getDownloadCount());
            pstmt.setInt(8, report.getReportId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete report
    public boolean deleteReport(int reportId) {
        String sql = "DELETE FROM reports WHERE report_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reportId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Export report
    public boolean exportReport(int reportId, String format) {
        String sql = "UPDATE reports SET download_count = download_count + 1 WHERE report_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reportId);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get reports count
    public int getReportsCount() {
        String sql = "SELECT COUNT(*) FROM reports";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setReportId(rs.getInt("report_id"));
        report.setReportType(rs.getString("report_type"));
        report.setTitle(rs.getString("title"));
        report.setDescription(rs.getString("description"));
        report.setContent(rs.getString("content"));
        report.setGeneratedBy(rs.getString("generated_by"));
        report.setGenerationDate(rs.getDate("generation_date").toLocalDate());
        report.setLevel(rs.getString("level"));
        report.setDepartment(rs.getString("department"));
        report.setDownloadCount(rs.getInt("download_count"));
        return report;
    }
}