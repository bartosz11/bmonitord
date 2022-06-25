package me.bartosz1.monitoring;

import me.bartosz1.monitoring.models.Monitor;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

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

    //ok man i know this is the most stupid way to solve "MultipleBagFetchException" but i don't give a shit at this point
    //I'm desperately trying to fix it for 2 hours
    public static List<Monitor> mergeMonitorLists(List<Monitor> firstQuery, List<Monitor> secondQuery) {
        List<Monitor> newList = new ArrayList<>();
        for (int i = 0; i < firstQuery.size(); i++) {
            newList.add(firstQuery.get(i).setContactLists(secondQuery.get(i).getContactLists()));
        }
        return newList;
    }
}
