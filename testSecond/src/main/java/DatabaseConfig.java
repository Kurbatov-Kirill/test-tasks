import java.io.File;
import java.nio.file.Files;
import java.sql.*;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "qwedsazxc";

        return DriverManager.getConnection(url, user, password);
    }

    public static void saveFileInfo(String originalName, String savedName, int viewsCount, int downloadsCount) {
        String sql = "INSERT INTO uploaded_files (original_name, saved_name, views_count, downloads_count) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, originalName);
            pstmt.setString(2, savedName);
            pstmt.setInt(3, viewsCount);
            pstmt.setInt(4, downloadsCount);
            pstmt.executeUpdate();
            System.out.println("Данные файла сохранены в БД");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cleanOldFiles() {
        String selectSql = "SELECT saved_name FROM uploaded_files WHERE last_download_at < NOW() - INTERVAL '30 days'";
        String deleteSql = "DELETE FROM uploaded_files WHERE last_download_at < NOW() - INTERVAL '30 days'";
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {

            while (rs.next()) {
                String fileName = rs.getString("saved_name");
                File file = new File(uploadPath + File.separator + fileName);
                if (file.exists()) {
                    Files.delete(file.toPath());
                    System.out.println("Удален старый файл: " + fileName);
                }
            }

            int deletedRows = stmt.executeUpdate(deleteSql);
            System.out.println("Удалено записей из БД: " + deletedRows);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateLastDownloadDate(String savedName) {
        String sql = "UPDATE uploaded_files SET last_download_at = NOW() WHERE saved_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, savedName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}