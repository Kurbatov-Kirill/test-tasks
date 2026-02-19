package com.github.kurbatov.filehoster.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

// Сервлет для отображения странички со статистикой по файлу, который хотят скачать
public class ViewServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String fileId = request.getParameter("id");
        StatisticsService statsService;

        try {
            statsService = new StatisticsService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Stats stats = statsService.getStats((fileId));

        request.setAttribute("fileStats", stats);
        request.getRequestDispatcher("/stats.jsp").forward(request, response);
    }
}
