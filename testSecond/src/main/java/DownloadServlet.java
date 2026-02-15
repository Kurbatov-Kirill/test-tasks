import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = request.getParameter("id");
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        File file = new File(uploadPath + File.separator + fileName);

        if (file.exists()) {
            DatabaseConfig.updateLastDownloadDate(fileName);

            response.setContentType(getServletContext().getMimeType(fileName));
            response.setContentLength((int) file.length());

            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName +"\"");
            java.nio.file.Files.copy(file.toPath(), response.getOutputStream());
        } else {
            response.sendError(404, "Файл не найден");
        }
    }
}