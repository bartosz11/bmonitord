package me.bartosz1.monitoring;

import java.security.SecureRandom;

public class Utils {
    private static final SecureRandom random = new SecureRandom();
    public static String generateRandomString(int length) {
        int leftLimit = 48; // letter 'a'
        int rightLimit = 122; // letter 'z'
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
