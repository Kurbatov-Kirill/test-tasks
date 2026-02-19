package com.github.kurbatov.filehoster.servlets;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Данные из properties для коннекта к БД
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

        String user = request.getParameter("username");
        String pass = request.getParameter("password");

        try {
            // Хэш
            /*MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pass.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));*/

            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {

                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO usrs (username, password) VALUES (?, ?)")) {
                    ps.setString(1, user);
                    ps.setString(2, pass);  // регистрация без хеша, должно быть иначе
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO user_roles (username, role_name) VALUES (?, 'USER')")) {
                    ps.setString(1, user);
                    ps.executeUpdate();
                }
            }
            response.sendRedirect("login.html?success=true");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(500, "Ошибка регистрации: " + e.getMessage());
        }
    }
}