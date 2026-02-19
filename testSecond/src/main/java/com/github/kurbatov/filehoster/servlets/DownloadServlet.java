package com.github.kurbatov.filehoster.servlets;

import com.github.kurbatov.filehoster.config.DatabaseConfig;
import com.github.kurbatov.filehoster.services.StatisticsService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

// Сервлет для скачивания файла с сервера
public class DownloadServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Получаем информацию о запрашиваемом файле
        String fileName = request.getParameter("id");
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        File file = new File(uploadPath + File.separator + fileName);

        StatisticsService statsService;

        try {
            statsService = new StatisticsService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Ищем файл, если он существует
        if (file.exists()) {
            // то обновляем дату последнего скачивания и повышаем счётчик количества скачиваний
            DatabaseConfig.updateLastDownloadDate(fileName);
            statsService.incrementDownloads(fileName);

            // Скачиваем файл
            response.setContentType(getServletContext().getMimeType(fileName));
            response.setContentLength((int) file.length());

            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName +"\"");
            java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
        } else {
            response.sendError(404, "Файл не найден");
        }
    }
}