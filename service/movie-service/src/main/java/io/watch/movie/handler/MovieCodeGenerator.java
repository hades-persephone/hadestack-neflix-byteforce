package io.watch.movie.handler;

import io.watch.basedata.util.CommonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.watch.basedata.util.CommonUtil.generateRandomString;

@Service
public class MovieCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String PREFIX = "HSNB";
    private static final String UNKNOWN = "UNK";
    private static final SecureRandom random = new SecureRandom();
    private static final String REGEX = "[^A-Z]";
    public static final String SPLIT_REGEX = "\\s+";

    public String generateMeaningfulCode(String title, Integer releaseYear) {
        String titleCode = sanitizeTitle(title);
        String yearCode = releaseYear != null ? releaseYear.toString() : UNKNOWN;
        String randomSuffix = generateRandomString(4);
        
        return PREFIX + "-" + titleCode + "-" + yearCode + "-" + randomSuffix;
    }
    
    public String generateShortMeaningfulCode(String title, Integer releaseYear) {
        String titleCode = extractTitleCode(title);
        String yearCode = releaseYear != null ? String.valueOf(releaseYear % 100) : "00";
        String randomSuffix = generateRandomString(4);

        return PREFIX + "-" + titleCode + yearCode + "-" + randomSuffix;
    }
    
    public String generateUUIDCode() {
        String uuid = UUID.randomUUID().toString();
        return PREFIX + "-" + uuid;
    }
    
    public String generateGenreSequentialCode(String genre, Integer releaseYear, Long sequenceInGenre) {
        String genreCode = genre.toUpperCase().replaceAll(REGEX, "");
        String yearCode = releaseYear != null ? releaseYear.toString() : UNKNOWN;
        String reqCode = String.format("%03d", sequenceInGenre);
        
        return PREFIX + "-" + genreCode + "-" + yearCode + "-" + reqCode;
    }
    
    public String generateDirectorBasedCode(String director, String title, Integer releaseYear) {
        String directorCode = extractDirectorCode(director);
        String titleCode = sanitizeTitle(title);
        String yearCode = releaseYear != null ? releaseYear.toString() : UNKNOWN;
        
        return PREFIX + "-" + directorCode + "-" + titleCode + "-" + yearCode;
    }
    
    public String generateNetflixStyleCode(String title, Integer releaseYear) {
        String input = title + (releaseYear != null ? releaseYear : "");
        int hash = Math.abs(input.hashCode());
        String code = String.format("%08d", hash % 100000000);
        
        return PREFIX + "-" + code;
    }

    public String generateCategoryBasedCode(String category, String title, Integer releaseYear) {
        String categoryCode = category.toUpperCase().replaceAll(REGEX, "");
        String titleCode = sanitizeTitle(title);
        String yearCode = releaseYear != null ? releaseYear.toString() : UNKNOWN;

        return PREFIX + "-" + categoryCode + "-" + titleCode + "-" + yearCode;
    }

    public String generateTimestampCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = generateRandomString(6);
        return PREFIX + "-" + timestamp + "-" + randomSuffix;
    }

    public String generateSequentialCode(Long sequenceId) {
        return PREFIX + "-" + String.format("%06d", sequenceId);
    }

    public String generateHashedCode(String title, Integer releaseYear, String director) {
        String input = title + releaseYear + director;
        String hash = DigestUtils.md5DigestAsHex(input.getBytes());
        return PREFIX + "-" + hash.substring(0, 12).toUpperCase();
    }

    public String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder(PREFIX + "-");
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHABET.length());
            code.append(ALPHABET.charAt(index));
        }
        return code.toString();
    }

    public String generateBase62Code() {
        long timestamp = System.currentTimeMillis();
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 9999);
        long combined = timestamp + randomPart;
        return PREFIX + "-" + toBase62(combined);
    }

    public String generateGenreBasedCode(String genre) {
        String sanitizedGenre = genre.toUpperCase().replaceAll(REGEX, "");
        String randomSuffix = generateRandomString(6);
        return PREFIX + "-" + sanitizedGenre + "-" + randomSuffix;
    }

    private String extractTitleCode(String title) {
        if(title == null || title.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        String[] words = title.toUpperCase().split(SPLIT_REGEX);
        StringBuilder code = new StringBuilder();
        
        for(String word : words) {
            if(word.length() > 2) {
                code.append(word, 0, 3);
                if(code.length() >= 9) break;
            }
        }
        
        return !code.isEmpty() ? code.toString() : UNKNOWN;
    }

    private String sanitizeTitle(String title) {
        if(title == null || title.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String[] commonWord = {""};
        String[] words = title.toLowerCase().split(SPLIT_REGEX);
        
        StringBuilder result = new StringBuilder();
        for(String word : words) {
            if(word.length() > 1 && !containsIgnoreCase(commonWord, word)) {
                result.append(word.toUpperCase().replaceAll(REGEX, ""));
            }
        }
        String sanitized = result.toString();
        if(sanitized.length() < 3) {
            sanitized = title.toUpperCase().replaceAll(REGEX, "");
        }
        return sanitized.length() > 10 ? sanitized.substring(0, 10) : sanitized;
    }

    private boolean containsIgnoreCase(String[] words, String target) {
        for(String word : words) {
            if(word.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private String extractDirectorCode(String director) {
        if (director == null || director.trim().isEmpty()) {
            return "UNKNOWN";
        }

        String[] names = director.toUpperCase().split(SPLIT_REGEX);
        String lastName = names[names.length - 1].replaceAll(REGEX, "");

        return lastName.length() > 8 ? lastName.substring(0, 8) : lastName;
    }

    private String toBase62(long value) {
        if (value == 0) return "0";

        String alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();

        while (value > 0) {
            result.append(alphabet.charAt((int) (value % 62)));
            value /= 62;
        }

        return result.reverse().toString();
    }

    public boolean isValidCode(String code) {
        return code != null && code.startsWith(PREFIX + "-") && code.length() > 3;
    }
    
}
