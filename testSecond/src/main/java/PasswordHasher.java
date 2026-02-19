import org.apache.catalina.realm.MessageDigestCredentialHandler;

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