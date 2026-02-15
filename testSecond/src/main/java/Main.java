import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws LifecycleException {

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn != null) {
                System.out.println("Успешное подключение к PostgreSQL!");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
        }

        Tomcat tomcat = new Tomcat();

        tomcat.setPort(8080);
        tomcat.getConnector();

        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);

        Context ctx = tomcat.addWebapp("", webappDir.getAbsolutePath());

        Wrapper servletWrapper = Tomcat.addServlet(ctx, "UploadServlet", new UploadServlet());
        servletWrapper.addMapping("/upload");
        // Для Multipart вручную:
        servletWrapper.setMultipartConfigElement(new MultipartConfigElement(""));
        Tomcat.addServlet(ctx, "DownloadServlet", new DownloadServlet());
        ctx.addServletMappingDecoded("/download", "DownloadServlet");

        System.out.println("Запуск Tomcat...");
        tomcat.start();
        tomcat.getServer().await();
    }
}