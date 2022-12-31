package me.bartosz1.monitoring;

import java.util.Random;

public class TextUtils {

    public static String generateAuthSalt() {
        int leftLimit = 48;
        int rightLimit = 122;
        Random random = new Random();
        //11 is max length, 8 is min
        int targetStringLength = random.nextInt(11 - 8) + 8;
        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
