package com.github.kurbatov.filehoster;

import com.github.kurbatov.filehoster.config.DatabaseConfig;
import com.github.kurbatov.filehoster.servlets.*;
import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.realm.DataSourceRealm;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws LifecycleException, NoSuchAlgorithmException, IOException {
        // Проверяем и настраиваем структуру БД
        initDatabase();

        // Настраиваем повторяющуюся задачу. Каждый час проверяем, появились ли файлы, у которых истёк срок хранения
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(DatabaseConfig::cleanOldFiles, 0, 1, TimeUnit.HOURS);

        Tomcat tomcat = new Tomcat();

        tomcat.enableNaming();
        tomcat.setPort(8080);
        tomcat.getConnector();

        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);

        Context ctx = tomcat.addWebapp("", webappDir.getAbsolutePath());
        ctx.setParentClassLoader(Main.class.getClassLoader());

        // Получаем данные для подключения к БД из файла properties, настраиваем взаимодействие и подключение к БД
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

        ContextResource res = new ContextResource();
        res.setName("jdbc/UsersDB");
        res.setType("javax.sql.DataSource");
        res.setAuth("Container");
        res.setProperty("driverClassName", "org.postgresql.Driver");
        res.setProperty("url", dbUrl);
        res.setProperty("username", dbUser);
        res.setProperty("password", dbPass);
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

        // Снова кусок от попытки реализации хэшей
        /*MessageDigestCredentialHandler handler = new MessageDigestCredentialHandler();
        handler.setAlgorithm("SHA-256");
        handler.setEncoding("UTF-8");
        handler.setSaltLength(0);
        realm.setCredentialHandler(handler);*/

        ctx.setRealm(realm);

        link.setName("jdbc/MyDatabase");
        link.setGlobal("jdbc/UsersDB");
        link.setType("javax.sql.DataSource");
        ctx.getNamingResources().addResourceLink(link);

        // Регистрируем и добавляем в приложение сервлеты
        Tomcat.addServlet(ctx, "com.github.kurbatov.filehoster.servlets.RegisterServlet", new RegisterServlet());
        ctx.addServletMappingDecoded("/do-register", "com.github.kurbatov.filehoster.servlets.RegisterServlet");

        Wrapper servletWrapper = Tomcat.addServlet(ctx, "com.github.kurbatov.filehoster.servlets.UploadServlet", new UploadServlet());
        ctx.addServletMappingDecoded("/upload", "com.github.kurbatov.filehoster.servlets.UploadServlet");
        // Для Multipart вручную:
        servletWrapper.setMultipartConfigElement(new MultipartConfigElement(""));
        Tomcat.addServlet(ctx, "com.github.kurbatov.filehoster.servlets.DownloadServlet", new DownloadServlet());
        ctx.addServletMappingDecoded("/download", "com.github.kurbatov.filehoster.servlets.DownloadServlet");

        Tomcat.addServlet(ctx, "com.github.kurbatov.filehoster.servlets.ViewServlet", new ViewServlet());
        ctx.addServletMappingDecoded("/view", "com.github.kurbatov.filehoster.servlets.ViewServlet");

        Tomcat.addServlet(ctx, "com.github.kurbatov.filehoster.servlets.LogoutServlet", new LogoutServlet());
        ctx.addServletMappingDecoded("/logout", "com.github.kurbatov.filehoster.servlets.LogoutServlet");

        System.out.println("Запуск Tomcat...");

        tomcat.getConnector().setURIEncoding("UTF-8");
        ctx.addParameter("org.apache.catalina.filters.SetCharacterEncodingFilter.encoding", "UTF-8");
        tomcat.start();
        System.out.println("Приложение запущено!");
        tomcat.getServer().await();
    }

    // Проверяем, есть ли нужные таблицы в БД исоздаем их при необходимости
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
        } catch (SQLException | IOException e) {
            System.err.println("Ошибка инициализации таблицы: " + e.getMessage());
        }

        sql = "CREATE TABLE IF NOT EXISTS usrs (" +
                "username VARCHAR(50) NOT NULL PRIMARY KEY," +
                "password VARCHAR(64) NOT NULL)";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Таблица с пользователями проверена/создана успешно.");
        } catch (SQLException | IOException e) {
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
        } catch (SQLException | IOException e) {
            System.err.println("Ошибка инициализации таблицы: " + e.getMessage());
        }
    }
}