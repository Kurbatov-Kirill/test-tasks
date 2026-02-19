package com.github.kurbatov.weather;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        String containerName = "weather-redis";

        try {
            System.out.println("Проверка образа Redis...");
            dockerClient.pullImageCmd("redis")
                    .withTag("latest")
                    .start()
                    .awaitCompletion(30, TimeUnit.SECONDS);
            System.out.println("Образ готов.");

            boolean containerExists = dockerClient.listContainersCmd().withShowAll(true).exec()
                    .stream().anyMatch(c -> Arrays.asList(c.getNames()).contains("/" + containerName));

            if (!containerExists) {
                System.out.println("Создание нового контейнера Redis...");
                dockerClient.createContainerCmd("redis:latest")
                        .withName(containerName)
                        .withHostConfig(new HostConfig().withPortBindings(new Ports(PortBinding.parse("6379:6379"))))
                        .exec();
            }

            System.out.println("Запуск контейнера " + containerName + "...");
            dockerClient.startContainerCmd(containerName).exec();

            System.out.println("Ожидание инициализации Redis...");
            boolean isReady = false;
            for (int i = 0; i < 10; i++) {
                try (Jedis jedis = new Jedis("localhost", 6379)) {
                    if ("PONG".equals(jedis.ping())) {
                        isReady = true;
                        break;
                    }
                } catch (Exception e) {
                    Thread.sleep(1000);
                }
            }

            if (isReady) {
                System.out.println("Redis готов к работе");
            } else {
                System.err.println("Redis не ответил. Проверьте Docker Desktop");
            }

        } catch (Exception e) {
            System.err.println("Ошибка Docker: " + e.getMessage());
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);

        String webappDir = new File("src/main/webapp").getAbsolutePath();
        Context context = tomcat.addWebapp("", webappDir);

        Tomcat.addServlet(context, "com.github.kurbatov.weather.WeatherServlet", new WeatherServlet());
        context.addServletMappingDecoded("/weather", "com.github.kurbatov.weather.WeatherServlet");

        System.out.println("\n========================================");
        System.out.println("Приложение запущено: http://localhost:8080");
        System.out.println("========================================\n");

        tomcat.getConnector().setURIEncoding("UTF-8");
        context.addParameter("org.apache.catalina.filters.SetCharacterEncodingFilter.encoding", "UTF-8");
        tomcat.start();
        tomcat.getServer().await();
    }
}