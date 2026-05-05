package Util;

import model.TaiKhoan;

public class Session {
    private static TaiKhoan currentUser;

    public static void setUser(TaiKhoan user) { currentUser = user; }
    public static TaiKhoan getUser()           { return currentUser; }
    public static void clear()                 { currentUser = null; }

    public static boolean isAdmin() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getIdNhanVien() : 0;
    }
}