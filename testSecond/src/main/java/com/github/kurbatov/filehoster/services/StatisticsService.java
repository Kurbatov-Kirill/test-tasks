package com.github.kurbatov.filehoster.services;

import com.github.kurbatov.filehoster.Stats;
import com.github.kurbatov.filehoster.config.DatabaseConfig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Сервис обработки статистики
public class StatisticsService {

    public StatisticsService() throws SQLException {
    }

    // Получить всю стату
    public Stats getStats(String fileId) {
        String sql = "SELECT * FROM uploaded_files WHERE (saved_name = ?)";
        Stats stats = new Stats();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fileId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime uploadTime = rs.getObject("upload_date", LocalDateTime.class);
                    LocalDateTime lastDownloadTime = rs.getObject("last_download_at", LocalDateTime.class);
                    LocalDateTime terminationDeadline = lastDownloadTime.plusDays(30);
                    String formattedUploadTime = uploadTime.format(formatter);
                    String formattedTerminationDate = terminationDeadline.format(formatter);
                    String formattedLastDownloadDate = lastDownloadTime.format(formatter);

                    stats.setFileId(fileId);
                    stats.setFilename(rs.getString("original_name"));
                    stats.setDownloadsCount(rs.getInt("downloads_count"));
                    stats.setViewsCount(rs.getInt("views_count") + 1);
                    stats.setDownloadsCount(rs.getInt("downloads_count"));
                    stats.setUploadTime(uploadTime);
                    stats.setLastDownloadTime(lastDownloadTime);
                    stats.setTerminationTime(terminationDeadline);
                    stats.setDisplayingLastDownloadTime(formattedLastDownloadDate);
                    stats.setDisplayingTerminationTime(formattedTerminationDate);
                    stats.setDisplayingUploadTime(formattedUploadTime);
                }
            }
            // Сразу повышаем счётчик просмотров
            sql = "UPDATE uploaded_files SET views_count = ? WHERE saved_name = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(sql);

            pstmt2.setInt(1, stats.getViewsCount());
            pstmt2.setString(2, fileId);

            pstmt2.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    // Для повышения счётчика загрузок
    public void incrementDownloads(String fileId) {
        String sql = "UPDATE uploaded_files SET downloads_count = downloads_count + 1 WHERE saved_name = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fileId);
            pstmt.executeUpdate();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
