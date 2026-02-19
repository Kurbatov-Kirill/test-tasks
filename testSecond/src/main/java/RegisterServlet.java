import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.security.MessageDigest;

@WebServlet("/do-register")
public class RegisterServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "qwedsazxc";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String user = request.getParameter("username");
        String pass = request.getParameter("password");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pass.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));

            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {

                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO usrs (username, password) VALUES (?, ?)")) {
                    ps.setString(1, user);
                    ps.setString(2, pass);  // регистрация без хеша
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