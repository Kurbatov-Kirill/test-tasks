import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;

@WebServlet("/upload")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class UploadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Part filePart = request.getPart("fileToUpload");
            if (filePart == null) {
                throw new Exception("Поле 'fileToUpload' не найдено в запросе");
            }

            String fileId = java.util.UUID.randomUUID().toString();
            String originalName = filePart.getSubmittedFileName();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedFileName = fileId + extension;

            String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            filePart.write(uploadPath + File.separator + savedFileName);

            response.setStatus(200);
            response.getWriter().write(savedFileName);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().write("Server Error: " + e.getMessage());
        }
    }
}