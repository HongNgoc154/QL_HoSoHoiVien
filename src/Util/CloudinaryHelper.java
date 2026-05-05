package Util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.File;
import java.util.Map;

/**
 * Helper upload ảnh lên Cloudinary
 * Cấu hình cloud_name, api_key, api_secret theo tài khoản của bạn
 */
public class CloudinaryHelper {

    // ===== CẤU HÌNH CLOUDINARY =====
    // Thay các giá trị này bằng thông tin tài khoản Cloudinary của bạn
    private static final String CLOUD_NAME = readConfig("CLOUDINARY_CLOUD_NAME", "cloudinary.cloud_name", "dfmzybrqb");
    private static final String API_KEY    = readConfig("CLOUDINARY_API_KEY", "cloudinary.api_key", "888891812948818");
    private static final String API_SECRET = readConfig("CLOUDINARY_API_SECRET", "cloudinary.api_secret", "NN70mI2g2c3R7Mh0raZ-o2cfWts");

    private static Cloudinary cloudinary;
    
    private static String readConfig(String envKey, String propKey, String fallback) {
        String v = System.getenv(envKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getProperty(propKey);
        }
        if (v == null || v.trim().isEmpty()) {
            return fallback;
        }
        return v.trim();
    }

    private static boolean isConfigured() {
        return !"your_cloud_name".equals(CLOUD_NAME)
            && !"your_api_key".equals(API_KEY)
            && !"your_api_secret".equals(API_SECRET);
    }


    private static Cloudinary getInstance() {
        if (cloudinary == null) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key",    API_KEY,
                "api_secret", API_SECRET,
                "secure",     true
            ));
        }
        return cloudinary;
    }

    /**
     * Upload file ảnh lên Cloudinary, trả về URL public
     * @param imageFile file ảnh cần upload
     * @param folder    thư mục lưu trên Cloudinary (vd: "hoivien")
     * @return URL ảnh trên cloud, hoặc null nếu thất bại
     */
    @SuppressWarnings("unchecked")
    public static String uploadImage(File imageFile, String folder) {
        try {
            if (!isConfigured()) {
                System.err.println("Cloudinary chưa được cấu hình. Hãy đặt biến môi trường CLOUDINARY_CLOUD_NAME/CLOUDINARY_API_KEY/CLOUDINARY_API_SECRET hoặc Java system properties cloudinary.cloud_name/cloudinary.api_key/cloudinary.api_secret.");
                return null;
            }
            Map<?, ?> result = getInstance().uploader().upload(
                imageFile,
                ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",

                    // 🔥 tối ưu tốc độ upload
                    "quality", "auto:eco",   // nén mạnh hơn
                    "fetch_format", "auto",

                    // 🔥 resize trước khi upload
                    "width", 800,
                    "crop", "limit"
                )
            );
            return (String) result.get("secure_url");
        } catch (Exception e) {
            System.err.println("Lỗi upload Cloudinary: " + e.getMessage());
            return null;
        }
    }

    /**
     * Kiểm tra file có phải ảnh hợp lệ không
     */
    public static boolean isValidImage(File file) {
        if (file == null || !file.exists()) return false;
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg")
            || name.endsWith(".png") || name.endsWith(".gif")
            || name.endsWith(".webp");
    }

    /**
     * Lấy thumbnail URL từ URL gốc (resize 100x100)
     */
    public static String getThumbnailUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) return null;
        // Cloudinary transformation: c_fill,w_100,h_100
        return originalUrl.replace("/upload/", "/upload/c_fill,w_100,h_100/");
    }
}