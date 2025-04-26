package io.watch.search.util;

public class KafkaUtil {

    public static String generateKey(Long id) {
        return String.valueOf(id);
    }
}