package com.github.kurbatov.filehoster.config;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.util.Properties;

// Класс для основных операций с БД
public class DatabaseConfig {
    public static Connection getConnection() throws SQLException, IOException {
        Properties props = new Properties();

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new FileNotFoundException("Файл не найден в classpath");
            }
            props.load(is);
        }
        String dbUrl = props.getProperty("db.url");
        String dbUser = props.getProperty("db.user");
        String dbPass = props.getProperty("db.password");

        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    // Сохранение информации о загруженном файле
    public void saveFileInfo(String originalName, String savedName, int viewsCount, int downloadsCount) {
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

    // Удаление файлов, которые не скачивали более 30 дней
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
    // Обновление даты последнего скачивания
    public static void updateLastDownloadDate(String savedName) {
        String sql = "UPDATE uploaded_files SET last_download_at = NOW() WHERE saved_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, savedName);
            pstmt.executeUpdate();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}