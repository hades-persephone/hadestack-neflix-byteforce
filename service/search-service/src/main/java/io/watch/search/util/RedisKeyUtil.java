package io.watch.search.util;

public class RedisKeyUtil {

    private static final String PREFERENCE_KEY_PREFIX = "user:preference:";

    public static String getPreferenceKey(Long userId, Long profileId) {
        return PREFERENCE_KEY_PREFIX + userId + ":" + profileId;
    }
}