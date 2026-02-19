package com.github.kurbatov.weather;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherServlet extends HttpServlet {

    private static final JedisPool jedisPool = new JedisPool("localhost", 6379);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String city = req.getParameter("city");

        if (city == null || city.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "City is required");
            return;
        }

        String cacheKey = "weather:" + city.toLowerCase().trim();
        String finalJson;

        try (Jedis jedis = jedisPool.getResource()) {
            finalJson = jedis.get(cacheKey);

            if (finalJson == null) {
                System.out.println("Кэш пуст. Запрашиваем API для города: " + city);

                String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
                        + URLEncoder.encode(city, "UTF-8");
                System.out.println("DEBUG: Запрос геокодинга -> " + geoUrl + "&count=1&language=ru");
                String geoResponse = makeHttpRequest(geoUrl);

                if (geoResponse.contains("\"results\":")) {
                    double lat = extractValue(geoResponse, "latitude");
                    double lon = extractValue(geoResponse, "longitude");

                    String weatherUrl = String.format(
                            "https://api.open-meteo.com/v1/forecast?latitude=" +
                            lat + "&longitude=" + lon + "&hourly=temperature_2m&forecast_hours=24"
                    );
                    System.out.println("DEBUG: Запрос погоды -> " + weatherUrl);
                    finalJson = makeHttpRequest(weatherUrl);
                    finalJson = replace(finalJson);
                    jedis.setex(cacheKey, 900, finalJson);
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "City not found");
                    return;
                }
            } else {
                System.out.println("Данные получены из Redis для: " + city);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(finalJson);
    }

    private String makeHttpRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private double extractValue(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern) + pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Double.parseDouble(json.substring(start, end));
    }
    private String replace(String node) {
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}");
        Matcher matcher = pattern.matcher(node);

        DateTimeFormatter isoFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        String result = matcher.replaceAll(mr -> {
            LocalDateTime dt = LocalDateTime.parse(mr.group(), isoFormat);
            return dt.plusHours(3).format(isoFormat);
        });
        return result;
    }
}