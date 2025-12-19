package DAO;

import Model.PasswordResetToken;

public interface PasswordResetDAO {

    // Create a new verification code for a user (overwrites any active one)
    boolean createToken(int userId, String verificationCode);

    // Get token by verification code (only if not used and not expired)
    PasswordResetToken getTokenByCode(String verificationCode);

    // Mark token as used after successful reset
    boolean markTokenAsUsed(int tokenId);

    // Get active token for a user (for validation)
    PasswordResetToken getActiveTokenForUser(int userId);
}