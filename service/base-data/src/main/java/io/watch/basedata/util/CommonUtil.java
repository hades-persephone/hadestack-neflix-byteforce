package io.watch.basedata.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@UtilityClass
@Slf4j
public class CommonUtil {

    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Phone pattern (Vietnamese format)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+84|84|0)+(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$"
    );

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            .registerModule(new JavaTimeModule());

    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    public String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }

    public LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    public LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
    }

    // ==== 🔐 Encoding / Decoding ====
    public String encodeBase64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    public String decodeBase64(String encoded) {
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    public String urlEncode(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    public String urlDecode(String input) {
        return URLDecoder.decode(input, StandardCharsets.UTF_8);
    }

    public boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }


    public boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public boolean isEmpty(Object obj) {
        if(obj == null) {
            return true;
        } else if(obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        } else if(obj instanceof Collection) {
            return isNullOrEmpty((Collection<?>) obj);
        }
        return false;
    }

    public boolean isNullOrEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String) return ((String) value).trim().isEmpty();
        if (value instanceof Collection) return ((Collection<?>) value).isEmpty();
        if (value instanceof Map) return ((Map<?, ?>) value).isEmpty();
        return false;
    }
    public boolean isNull(Object obj) {
        return obj == null;
    }

    public boolean isNotNull(Object obj) {
        return obj != null;
    }
    
    public String removeAccent(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    public String toSlug(String input) {
        if (input == null) return null;
        String noAccent = removeAccent(input);
        return noAccent.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("-+$", "").replaceAll("^-+", "");
    }

    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public int safeParseInt(String str, int defaultValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean isEmpty(String str) {
        return org.apache.commons.lang3.StringUtils.isEmpty(str);
    }

    public static boolean isNotEmpty(String str) {
        return org.apache.commons.lang3.StringUtils.isNotEmpty(str);
    }

    public static boolean isBlank(String str) {
        return org.apache.commons.lang3.StringUtils.isBlank(str);
    }

    public static boolean isNotBlank(String str) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(str);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return org.apache.commons.collections4.CollectionUtils.isEmpty(collection);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return org.apache.commons.collections4.CollectionUtils.isNotEmpty(collection);
    }

    public static <T> List<T> removeDuplicateAndNull(List<T> lst) {
        if (lst == null || lst.isEmpty()) {
            return Collections.emptyList();
        }
        Set<T> set = new HashSet<>();
        for (T t : lst) {
            if(t != null) {
                set.add(t);
            }
        }
        return new ArrayList<>(set);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return MapUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return MapUtils.isNotEmpty(map);
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * STRING CONVERSION METHODS (Enhanced with Apache Commons)
     */
    public static String toString(Object obj) {
        return obj == null ? org.apache.commons.lang3.StringUtils.EMPTY : obj.toString().trim();
    }

    public static String toString(Object obj, String defaultValue) {
        return obj == null ? org.apache.commons.lang3.StringUtils.defaultString(defaultValue) : obj.toString().trim();
    }

    public static String toUpperCase(String str) {
        return org.apache.commons.lang3.StringUtils.upperCase(str);
    }

    public static String toLowerCase(String str) {
        return org.apache.commons.lang3.StringUtils.lowerCase(str);
    }

    public static String capitalize(String str) {
        return org.apache.commons.lang3.StringUtils.capitalize(str);
    }

    public static String trim(String str) {
        return org.apache.commons.lang3.StringUtils.trim(str);
    }

    public static String trimToEmpty(String str) {
        return org.apache.commons.lang3.StringUtils.trimToEmpty(str);
    }

    public static String trimToNull(String str) {
        return org.apache.commons.lang3.StringUtils.trimToNull(str);
    }

    public static String abbreviate(String str, int maxWidth) {
        return org.apache.commons.lang3.StringUtils.abbreviate(str, maxWidth);
    }

    public static String leftPad(String str, int size, char padChar) {
        return org.apache.commons.lang3.StringUtils.leftPad(str, size, padChar);
    }

    public static String rightPad(String str, int size, char padChar) {
        return org.apache.commons.lang3.StringUtils.rightPad(str, size, padChar);
    }


    public static Integer toInteger(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return ((Number) obj).intValue();
            }
            return Integer.parseInt(obj.toString().trim());
        } catch (NumberFormatException e) {
            log.warn("Cannot convert {} to Integer", obj);
            return null;
        }
    }

    public static Integer toInteger(Object obj, Integer defaultValue) {
        Integer result = toInteger(obj);
        return result != null ? result : defaultValue;
    }

    public static Long toLong(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return ((Number) obj).longValue();
            }
            return Long.parseLong(obj.toString().trim());
        } catch (NumberFormatException e) {
            log.warn("Cannot convert {} to Long", obj);
            return null;
        }
    }

    public static Long toLong(Object obj, Long defaultValue) {
        Long result = toLong(obj);
        return result != null ? result : defaultValue;
    }

    public static Double toDouble(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            }
            return Double.parseDouble(obj.toString().trim());
        } catch (NumberFormatException e) {
            log.warn("Cannot convert {} to Double", obj);
            return null;
        }
    }

    public static Double toDouble(Object obj, Double defaultValue) {
        Double result = toDouble(obj);
        return result != null ? result : defaultValue;
    }

    public static BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return null;
        try {
            if (obj instanceof BigDecimal) {
                return (BigDecimal) obj;
            }
            if (obj instanceof Number) {
                return BigDecimal.valueOf(((Number) obj).doubleValue());
            }
            return new BigDecimal(obj.toString().trim());
        } catch (NumberFormatException e) {
            log.warn("Cannot convert {} to BigDecimal", obj);
            return null;
        }
    }
    public static Date toDate(String dateStr, String pattern) {
        if (isEmpty(dateStr) || isEmpty(pattern)) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr.trim());
        } catch (ParseException e) {
            log.warn("Cannot parse date {} with pattern {}", dateStr, pattern);
            return null;
        }
    }

    public static LocalDateTime toLocalDateTime(String dateStr, String pattern) {
        if (isEmpty(dateStr) || isEmpty(pattern)) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(dateStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            log.warn("Cannot parse LocalDateTime {} with pattern {}", dateStr, pattern);
            return null;
        }
    }

    public static LocalDate toLocalDate(String dateStr, String pattern) {
        if (isEmpty(dateStr) || isEmpty(pattern)) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDate.parse(dateStr.trim(), formatter);
        } catch (DateTimeParseException e) {
            log.warn("Cannot parse LocalDate {} with pattern {}", dateStr, pattern);
            return null;
        }
    }

    public static String formatNumber(Number number, String pattern) {
        if (number == null || isEmpty(pattern)) return "";
        try {
            DecimalFormat df = new DecimalFormat(pattern);
            return df.format(number);
        } catch (Exception e) {
            log.warn("Cannot format number {} with pattern {}", number, pattern);
            return number.toString();
        }
    }

    public static String formatDate(Date date, String pattern) {
        if (date == null || isEmpty(pattern)) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.format(date);
        } catch (Exception e) {
            log.warn("Cannot format date {} with pattern {}", date, pattern);
            return date.toString();
        }
    }

    public static String formatLocalDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || isEmpty(pattern)) return "";
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return dateTime.format(formatter);
        } catch (Exception e) {
            log.warn("Cannot format LocalDateTime {} with pattern {}", dateTime, pattern);
            return dateTime.toString();
        }
    }

    /**
     * VALIDATION METHODS
     */
    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isNumeric(String str) {
        if (isEmpty(str)) return false;
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInteger(String str) {
        if (isEmpty(str)) return false;
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String generateRandomString(int length) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(index));
        }
        return builder.toString();
    }

    public static String maskString(String str, int visibleStart, int visibleEnd, char maskChar) {
        if (isEmpty(str) || str.length() <= visibleStart + visibleEnd) {
            return str;
        }

        return str.substring(0, visibleStart) +
                String.valueOf(maskChar).repeat(Math.max(0, str.length() - visibleEnd - visibleStart)) +
                str.substring(str.length() - visibleEnd);
    }

    public static String maskEmail(String email) {
        if (!isValidEmail(email)) return email;

        int atIndex = email.indexOf("@");
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (username.length() <= 2) {
            return maskString(username, 1, 0, '*') + domain;
        } else {
            return maskString(username, 2, 1, '*') + domain;
        }
    }

    public static String maskPhone(String phone) {
        if (isEmpty(phone)) return phone;

        if (phone.length() <= 4) {
            return phone;
        }

        return maskString(phone, 3, 2, '*');
    }

    /**
     * COLLECTION UTILITIES (Enhanced with Apache Commons)
     */
    public static <T> List<T> safeList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    public static <K, V> Map<K, V> safeMap(Map<K, V> map) {
        return map == null ? new HashMap<>() : map;
    }

    public static <T> Set<T> safeSet(Set<T> set) {
        return set == null ? new HashSet<>() : set;
    }

    public static <T> Collection<T> intersection(Collection<T> a, Collection<T> b) {
        return org.apache.commons.collections4.CollectionUtils.intersection(a, b);
    }

    public static <T> Collection<T> union(Collection<T> a, Collection<T> b) {
        return org.apache.commons.collections4.CollectionUtils.union(a, b);
    }

    public static <T> Collection<T> subtract(Collection<T> a, Collection<T> b) {
        return org.apache.commons.collections4.CollectionUtils.subtract(a, b);
    }

    public static <T> boolean isEqualCollection(Collection<T> a, Collection<T> b) {
        return org.apache.commons.collections4.CollectionUtils.isEqualCollection(a, b);
    }

    /**
     * JSON UTILITIES (Jackson & Gson)
     */

    // Jackson Methods
    public static String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", e.getMessage());
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (isBlank(json)) return null;
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to object: {}", e.getMessage());
            return null;
        }
    }

    public static String toPrettyJson(Object obj) {
        if (obj == null) return null;
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to pretty JSON: {}", e.getMessage());
            return null;
        }
    }

    // Gson Methods (alternative)
    public static String toJsonGson(Object obj) {
        if (obj == null) return null;
        try {
            return GSON.toJson(obj);
        } catch (Exception e) {
            log.error("Error converting object to JSON using Gson: {}", e.getMessage());
            return null;
        }
    }

    public static <T> T fromJsonGson(String json, Class<T> clazz) {
        if (isBlank(json)) return null;
        try {
            return GSON.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            log.error("Error converting JSON to object using Gson: {}", e.getMessage());
            return null;
        }
    }

    /**
     * EQUALS UTILITIES
     */
    public static boolean equals(Object obj1, Object obj2) {
        return Objects.equals(obj1, obj2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return Objects.equals(str1, str2);
        }
        return str1.equalsIgnoreCase(str2);
    }
}
