package jm.edu.utech.helpdesk.service;
//created by rickayla mcneil 2111687
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

// hashes passwords
@Component
public class PasswordHasher {

    private final SecureRandom random = new SecureRandom();

    public String hash(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + digest(salt, password);
    }

    public boolean matches(String password, String storedHash) {
        if (password == null || storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] parts = storedHash.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String expected = parts[1];
        String actual = digest(salt, password);

        // compare
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private String digest(byte[] salt, String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(salt);
            byte[] hashed = messageDigest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static void main(String[] args) {
        System.out.println(new PasswordHasher().hash("Admin@123"));
    }
}
