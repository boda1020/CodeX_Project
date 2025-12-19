package DAO;

import Model.User;

public interface UserDAO {

    // Login using username or email
    User login(String identifier, String password);

    // Register new user (Admin or Student)
    boolean register(User user);

    // Find user by email (for forgot password)
    User findByEmail(String email);

    // Update password (for reset password)
    boolean updatePassword(int userId, String newPasswordPlain);

    // Check if username already exists
    boolean usernameExists(String username);

    // Check if email already exists
    boolean emailExists(String email);

    // جديد: تحديث كل بيانات اليوزر (للـ Profile)
    boolean updateUser(User user);

    // جديد: تحديث الصورة بس
    boolean updateProfileImage(int userId, String imagePath);
}