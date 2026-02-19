import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.realm.DataSourceRealm;
import org.apache.catalina.realm.MessageDigestCredentialHandler;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) throws LifecycleException, NoSuchAlgorithmException {

        initDatabase();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(DatabaseConfig::cleanOldFiles, 0, 1, TimeUnit.HOURS);

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn != null) {
                System.out.println("Успешное подключение к PostgreSQL!");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
        }

        Tomcat tomcat = new Tomcat();

        tomcat.enableNaming();
        tomcat.setPort(8080);
        tomcat.getConnector();

        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);

        Context ctx = tomcat.addWebapp("", webappDir.getAbsolutePath());


        ContextResource res = new ContextResource();
        res.setName("jdbc/UsersDB");
        res.setType("javax.sql.DataSource");
        res.setAuth("Container");
        res.setProperty("driverClassName", "org.postgresql.Driver");
        res.setProperty("url", "jdbc:postgresql://localhost:5432/postgres");
        res.setProperty("username", "postgres");
        res.setProperty("password", "qwedsazxc");
        res.setProperty("factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");

        tomcat.getServer().getGlobalNamingResources().addResource(res);

        ContextResourceLink link = new ContextResourceLink();

        DataSourceRealm realm = new DataSourceRealm();
        realm.setDataSourceName("jdbc/MyDatabase");
        realm.setUserTable("usrs");
        realm.setUserNameCol("username");
        realm.setUserCredCol("password");
        realm.setUserRoleTable("user_roles");
        realm.setRoleNameCol("role_name");

        MessageDigestCredentialHandler handler = new MessageDigestCredentialHandler();
        handler.setAlgorithm("SHA-256");
        handler.setEncoding("UTF-8");
        handler.setSaltLength(0);
        realm.setCredentialHandler(handler);

        ctx.setRealm(realm);

        link.setName("jdbc/MyDatabase");
        link.setGlobal("jdbc/UsersDB");
        link.setType("javax.sql.DataSource");
        ctx.getNamingResources().addResourceLink(link);

        Wrapper registerServlet = Tomcat.addServlet(ctx, "RegisterServlet", new RegisterServlet());
        registerServlet.addMapping("/do-register");

        Wrapper servletWrapper = Tomcat.addServlet(ctx, "UploadServlet", new UploadServlet());
        servletWrapper.addMapping("/upload");
        // Для Multipart вручную:
        servletWrapper.setMultipartConfigElement(new MultipartConfigElement(""));
        Tomcat.addServlet(ctx, "DownloadServlet", new DownloadServlet());
        ctx.addServletMappingDecoded("/download", "DownloadServlet");

        Tomcat.addServlet(ctx, "ViewServlet", new ViewServlet());
        ctx.addServletMappingDecoded("/view", "ViewServlet");

        System.out.println("Запуск Tomcat...");
        java.util.logging.Logger.getLogger("org.apache.catalina.realm").setLevel(java.util.logging.Level.ALL);
        ConsoleHandler сHandler = new ConsoleHandler();
        сHandler.setLevel(Level.ALL);

        tomcat.getConnector().setURIEncoding("UTF-8");
        ctx.addParameter("org.apache.catalina.filters.SetCharacterEncodingFilter.encoding", "UTF-8");

        System.out.println("Проверка привязки: " + ctx.getRealm().getCredentialHandler());
        tomcat.start();
        tomcat.getServer().await();
    }

    public static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS uploaded_files (" +
                "id SERIAL PRIMARY KEY, " +
                "original_name VARCHAR(255) NOT NULL, " +
                "saved_name VARCHAR(255) NOT NULL, " +
                "views_count INTEGER NOT NULL, " +
                "downloads_count INTEGER NOT NULL, " +
                "upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "last_download_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица для файлов проверена/создана успешно.");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации таблицы: " + e.getMessage());
        }

        sql = "CREATE TABLE IF NOT EXISTS usrs (" +
                "username VARCHAR(50) NOT NULL PRIMARY KEY," +
                "password VARCHAR(64) NOT NULL)";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица с пользователями проверена/создана успешно.");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации таблицы: " + e.getMessage());
        }

        sql = "CREATE TABLE IF NOT EXISTS user_roles (" +
                "username VARCHAR(50) NOT NULL, " +
                "role_name VARCHAR(20) NOT NULL, " +
                "FOREIGN KEY (username) REFERENCES usrs(username))";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица с ролями проверена/создана успешно.");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации таблицы: " + e.getMessage());
        }
    }
}