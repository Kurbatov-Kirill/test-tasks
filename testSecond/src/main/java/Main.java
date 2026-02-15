import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws LifecycleException {

        initDatabase();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // Запускаем задачу:
        // первый запуск через 0 секунд, повтор каждые 24 часа
        scheduler.scheduleAtFixedRate(DatabaseConfig::cleanOldFiles, 0, 1, TimeUnit.HOURS);

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

    public static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS uploaded_files (" +
                "id SERIAL PRIMARY KEY, " +
                "original_name VARCHAR(255) NOT NULL, " +
                "saved_name VARCHAR(255) NOT NULL, " +
                "upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_download_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица проверена/создана успешно.");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации таблицы: " + e.getMessage());
        }
    }
}