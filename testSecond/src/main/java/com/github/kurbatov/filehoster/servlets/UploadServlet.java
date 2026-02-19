package com.github.kurbatov.filehoster.servlets;

import com.github.kurbatov.filehoster.config.DatabaseConfig;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)

// Сервлет для загрузки файлов на сервер
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Получаем файл и формы
        try {
            Part filePart = request.getPart("fileToUpload");
            if (filePart == null) {
                throw new Exception("Поле 'fileToUpload' не найдено в запросе");
            }

            // Устанавливаем информацию по умолчанию для нового файла
            String fileId = UUID.randomUUID().toString();
            String originalName = filePart.getSubmittedFileName();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedFileName = fileId + extension;
            int viewsCount = 0;
            int downloadsCount = 0;

            // Загружаем сам файл в хранилище приложения
            String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            filePart.write(uploadPath + File.separator + savedFileName);

            response.setStatus(200);

            // Сохраняем информацию о файле в БД
            DatabaseConfig databaseConfig = new DatabaseConfig();

            databaseConfig.saveFileInfo(originalName, savedFileName, viewsCount, downloadsCount);
            response.getWriter().write(savedFileName);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("Server Error: " + e.getMessage());
        }
    }
}