package Utils;

public class CurrentUserSession {
    private static int userIdForPasswordReset = -1;

    public static void setUserIdForPasswordReset(int userId) {
        userIdForPasswordReset = userId;
    }

    public static int getUserIdForPasswordReset() {
        return userIdForPasswordReset;
    }

    public static void clearPasswordReset() {
        userIdForPasswordReset = -1;
    }
}