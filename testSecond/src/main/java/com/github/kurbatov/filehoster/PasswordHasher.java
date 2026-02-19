package com.github.kurbatov.filehoster;

import org.apache.catalina.realm.MessageDigestCredentialHandler;

// Не реализованный класс, попытка сделать хэш паролей в БД
// Успешная, но по какой-то причине авторизация Tomcat не хочет пускать, если сравниваются хеши
// только пароли как есть работают, так и не разобрался, почему
 public class PasswordHasher {
    public static String hashPassword(String password) {
        try {
            MessageDigestCredentialHandler generator = new MessageDigestCredentialHandler();
            generator.setAlgorithm("SHA-256");
            generator.setEncoding("UTF-8");
            System.out.println("HASH BY HASHER: " + generator.mutate(password));

            return generator.mutate(password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}