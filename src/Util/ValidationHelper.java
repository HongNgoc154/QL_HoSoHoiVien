package Util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Các hàm kiểm tra hợp lệ dữ liệu nhập
 */
public class ValidationHelper {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern SDT_PATTERN = Pattern.compile(
        "^(0|\\+84)(3[2-9]|5[6-9]|7[0|6-9]|8[0-9]|9[0-9])[0-9]{7}$"
    );

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Kiểm tra email hợp lệ
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Kiểm tra số điện thoại Việt Nam hợp lệ
     */
    public static boolean isValidSdt(String sdt) {
        return sdt != null && SDT_PATTERN.matcher(sdt.trim()).matches();
    }

    /**
     * Kiểm tra ngày sinh định dạng dd/MM/yyyy và hợp lệ
     * (không tương lai, không quá xa)
     */
    public static boolean isValidNgaySinh(String ngaySinh) {
        if (ngaySinh == null || ngaySinh.trim().isEmpty()) return false;
        try {
            LocalDate date = LocalDate.parse(ngaySinh.trim(), DATE_FORMAT);
            LocalDate now = LocalDate.now();
            // Phải nhỏ hơn ngày hiện tại và không quá 120 năm trước
            return date.isBefore(now) && date.isAfter(now.minusYears(120));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Chuyển từ dd/MM/yyyy → yyyy-MM-dd (để lưu SQL)
     */
    public static String toSqlDate(String displayDate) {
        try {
            LocalDate date = LocalDate.parse(displayDate.trim(), DATE_FORMAT);
            return date.toString(); // yyyy-MM-dd
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Chuyển từ yyyy-MM-dd → dd/MM/yyyy (để hiển thị)
     */
    public static String toDisplayDate(String sqlDate) {
        if (sqlDate == null || sqlDate.isEmpty()) return "";
        try {
            LocalDate date = LocalDate.parse(sqlDate.trim());
            return date.format(DATE_FORMAT);
        } catch (Exception e) {
            return sqlDate;
        }
    }

    /**
     * Kiểm tra trường bắt buộc không rỗng
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}