package io.watch.basedata.data.common;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lớp tiện ích cung cấp chức năng ánh xạ một tuple (mảng đối tượng) vào một Data Transfer Object (DTO)
 * dựa trên các alias được cung cấp.
 *
 * Lớp này sử dụng reflection để động ánh xạ dữ liệu từ tuple vào lớp DTO.
 * Nó cũng thực hiện chuyển đổi kiểu dữ liệu nếu cần thiết giữa kiểu dữ liệu của tuple và các trường tương ứng trong DTO.
 */
public class TupleToDtoMapper {

    /**
     * Ánh xạ các giá trị từ một tuple vào một lớp DTO.
     * Phương thức này lặp qua các giá trị trong tuple và ánh xạ chúng vào các trường trong DTO
     * dựa trên alias và tên trường. Nó cũng thực hiện chuyển đổi kiểu dữ liệu nếu cần thiết.
     *
     * @param tuple mảng chứa các giá trị cần được ánh xạ
     * @param aliases các alias trường tương ứng với các giá trị trong tuple
     * @param dtoClass lớp DTO mà các giá trị sẽ được ánh xạ vào
     * @param <T> kiểu của DTO
     * @return đối tượng DTO đã được gán giá trị từ tuple
     * @throws RuntimeException nếu có lỗi trong quá trình ánh xạ
     */
    public static <T> T mapTupleToDto(Object[] tuple, String[] aliases, Class<T> dtoClass) {
        try {
            // Tạo một instance mới của lớp DTO
            T instance = dtoClass.getDeclaredConstructor().newInstance();
            // Lấy tất cả các trường của lớp DTO
            Field[] fields = dtoClass.getDeclaredFields();

            // Tạo một map từ tên trường (chuyển thành chữ thường) tới các đối tượng Field
            Map<String, Field> fieldMap = Arrays.stream(fields)
                    .peek(f -> f.setAccessible(true)) // Làm các trường private có thể truy cập
                    .collect(Collectors.toMap(f -> f.getName().toLowerCase(), f -> f));

            // Lặp qua các alias và ánh xạ chúng vào các trường trong DTO
            for (int i = 0; i < aliases.length; i++) {
                String alias = aliases[i];
                Object value = tuple[i];

                // Bỏ qua alias null
                if (alias == null) continue;

                // Chuẩn hóa alias thành camel case và lấy trường tương ứng từ lớp DTO
                String normalizedAlias = toCamelCase(alias.toLowerCase());
                Field field = fieldMap.get(normalizedAlias.toLowerCase());

                if (field != null) {
                    // Chuyển đổi giá trị về kiểu dữ liệu phù hợp nếu cần thiết
                    if (value != null && !field.getType().isAssignableFrom(value.getClass())) {
                        value = convertValue(value, field.getType());
                    }
                    // Gán giá trị vào trường trong DTO
                    field.set(instance, value);
                }
            }

            return instance;
        } catch (Exception e) {
            // Ném ra ngoại lệ runtime nếu có lỗi trong quá trình ánh xạ
            throw new RuntimeException("Failed to map tuple to DTO: " + e.getMessage(), e);
        }
    }

    /**
     * Chuyển đổi một chuỗi thành camel case.
     * Chuyển đổi một chuỗi đầu vào (ví dụ: "first_name") thành camel case (ví dụ: "firstName").
     *
     * @param input chuỗi đầu vào cần chuyển đổi
     * @return camel case của chuỗi đầu vào
     */
    private static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) return input;

        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : input.toCharArray()) {
            if (c == '_' || c == '-' || c == '.' || c == ' ') {
                nextUpper = true;
            } else {
                result.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return result.toString();
    }


    /**
     * Chuyển đổi value sang targetType.
     * Chuyển đổi kiểu, chẳng hạn như từ String sang Integer, UUID, LocalDate, v.v.
     *
     * @param value giá trị cần chuyển đổi
     * @param targetType kiểu lớp mục tiêu mà giá trị cần được chuyển đổi
     * @return giá trị đã chuyển đổi
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        // Xử lý chuyển đổi sang các kiểu dữ liệu khác nhau
        if (targetType == String.class)
            return value.toString();
        if (targetType == UUID.class && value instanceof String)
            return UUID.fromString((String) value);
        if (targetType == Integer.class || targetType == int.class)
            return (value instanceof Number) ? ((Number) value).intValue() : Integer.parseInt(value.toString());
        if (targetType == Long.class || targetType == long.class)
            return (value instanceof Number) ? ((Number) value).longValue() : Long.parseLong(value.toString());
        if (targetType == Double.class || targetType == double.class)
            return (value instanceof Number) ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
        if (targetType == Float.class || targetType == float.class)
            return (value instanceof Number) ? ((Number) value).floatValue() : Float.parseFloat(value.toString());
        if (targetType == Short.class || targetType == short.class)
            return (value instanceof Number) ? ((Number) value).shortValue() : Short.parseShort(value.toString());
        if (targetType == Byte.class || targetType == byte.class)
            return (value instanceof Number) ? ((Number) value).byteValue() : Byte.parseByte(value.toString());
        if (targetType == Boolean.class || targetType == boolean.class)
            return (value instanceof Number) ? ((Number) value).intValue() != 0 : Boolean.parseBoolean(value.toString());
        if (targetType == LocalDate.class && value instanceof java.sql.Date)
            return ((java.sql.Date) value).toLocalDate();
        if (targetType == LocalDateTime.class && value instanceof java.sql.Timestamp)
            return ((java.sql.Timestamp) value).toLocalDateTime();

        return value;
    }
}
