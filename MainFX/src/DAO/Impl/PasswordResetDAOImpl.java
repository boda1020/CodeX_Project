package DAO.Impl;

import DAO.PasswordResetDAO;
import Model.PasswordResetToken;
import Database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;

public class PasswordResetDAOImpl implements PasswordResetDAO {

    @Override
    public boolean createToken(int userId, String verificationCode) {
        // Delete any existing active token for this user
        String deleteSql = "DELETE FROM password_reset_tokens WHERE user_id = ? AND used = FALSE";

        // Insert new token
        String insertSql = "INSERT INTO password_reset_tokens (user_id, verification_code) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, userId);
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, userId);
                insertStmt.setString(2, verificationCode);
                int rows = insertStmt.executeUpdate();
                conn.commit();
                return rows > 0;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PasswordResetToken getTokenByCode(String verificationCode) {
        String sql = "SELECT * FROM password_reset_tokens " +
                "WHERE verification_code = ? AND used = FALSE AND expires_at > NOW()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, verificationCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PasswordResetToken token = new PasswordResetToken();
                    token.setTokenId(rs.getInt("token_id"));
                    token.setUserId(rs.getInt("user_id"));
                    token.setVerificationCode(rs.getString("verification_code"));
                    token.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    token.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                    token.setUsed(rs.getBoolean("used"));
                    return token;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean markTokenAsUsed(int tokenId) {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE token_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, tokenId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public PasswordResetToken getActiveTokenForUser(int userId) {
        String sql = "SELECT * FROM password_reset_tokens " +
                "WHERE user_id = ? AND used = FALSE AND expires_at > NOW()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    PasswordResetToken token = new PasswordResetToken();
                    token.setTokenId(rs.getInt("token_id"));
                    token.setUserId(rs.getInt("user_id"));
                    token.setVerificationCode(rs.getString("verification_code"));
                    token.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    token.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                    token.setUsed(rs.getBoolean("used"));
                    return token;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}