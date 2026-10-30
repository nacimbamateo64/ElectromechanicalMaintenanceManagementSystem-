package ec.edu.espe.electromechanicalmaintenancemanagementsystem.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON reader/writer that only uses standard Java APIs.
 *
 * Parsed values are: Map (objects), List (arrays), String, Long, Double,
 * Boolean and null.
 */
public final class Json {

    private Json() {
    }

   
    public static Object parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Invalid JSON: the text is null.");
        }
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }
        Parser parser = new Parser(text);
        parser.skipWhitespace();
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (!parser.atEnd()) {
            throw parser.error("Unexpected content after the JSON value");
        }
        return value;
    }

    public static String string(Map<String, Object> object, String key) {
        Object value = object.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    public static int integer(Map<String, Object> object, String key, int defaultValue) {
        Integer value = optionalInteger(object, key);
        return value == null ? defaultValue : value;
    }

    public static Integer optionalInteger(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    public static float decimal(Map<String, Object> object, String key, float defaultValue) {
        Object value = object.get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            try {
                return Float.parseFloat(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static boolean bool(Map<String, Object> object, String key, boolean defaultValue) {
        Object value = object.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean(((String) value).trim());
        }
        return defaultValue;
    }

    public static List<Integer> integerList(Map<String, Object> object, String key) {
        List<Integer> result = new ArrayList<>();
        Object value = object.get(key);
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                if (item instanceof Number) {
                    result.add(((Number) item).intValue());
                }
            }
        }
        return result;
    }


    public static String write(Object value) {
        StringBuilder json = new StringBuilder();
        writeValue(json, value, 0);
        json.append('\n');
        return json.toString();
    }

    private static void writeValue(StringBuilder json, Object value, int level) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof String) {
            writeString(json, (String) value);
        } else if (value instanceof Boolean) {
            json.append(value.toString());
        } else if (value instanceof Float) {
            float number = (Float) value;
            json.append(Float.isFinite(number) ? Float.toString(number) : "null");
        } else if (value instanceof Double) {
            double number = (Double) value;
            json.append(Double.isFinite(number) ? Double.toString(number) : "null");
        } else if (value instanceof Number) {
            json.append(value.toString());
        } else if (value instanceof Map) {
            writeObject(json, (Map<?, ?>) value, level);
        } else if (value instanceof Collection) {
            writeArray(json, (Collection<?>) value, level);
        } else {
            writeString(json, String.valueOf(value));
        }
    }

    private static void writeObject(StringBuilder json, Map<?, ?> object, int level) {
        if (object.isEmpty()) {
            json.append("{}");
            return;
        }
        json.append("{\n");
        int index = 0;
        for (Map.Entry<?, ?> entry : object.entrySet()) {
            indent(json, level + 1);
            writeString(json, String.valueOf(entry.getKey()));
            json.append(": ");
            writeValue(json, entry.getValue(), level + 1);
            if (++index < object.size()) {
                json.append(',');
            }
            json.append('\n');
        }
        indent(json, level);
        json.append('}');
    }

    private static void writeArray(StringBuilder json, Collection<?> array, int level) {
        if (array.isEmpty()) {
            json.append("[]");
            return;
        }
        boolean simple = true;
        for (Object item : array) {
            if (item instanceof Map || item instanceof Collection) {
                simple = false;
                break;
            }
        }
        if (simple) {
            json.append('[');
            int index = 0;
            for (Object item : array) {
                if (index++ > 0) {
                    json.append(", ");
                }
                writeValue(json, item, level);
            }
            json.append(']');
            return;
        }
        json.append("[\n");
        int index = 0;
        for (Object item : array) {
            indent(json, level + 1);
            writeValue(json, item, level + 1);
            if (++index < array.size()) {
                json.append(',');
            }
            json.append('\n');
        }
        indent(json, level);
        json.append(']');
    }

    private static void indent(StringBuilder json, int level) {
        for (int i = 0; i < level; i++) {
            json.append("  ");
        }
    }

    private static void writeString(StringBuilder json, String value) {
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    json.append("\\\"");
                    break;
                case '\\':
                    json.append("\\\\");
                    break;
                case '\n':
                    json.append("\\n");
                    break;
                case '\r':
                    json.append("\\r");
                    break;
                case '\t':
                    json.append("\\t");
                    break;
                case '\b':
                    json.append("\\b");
                    break;
                case '\f':
                    json.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        json.append(String.format("\\u%04x", (int) c));
                    } else {
                        json.append(c);
                    }
            }
        }
        json.append('"');
    }


    private static final class Parser {
        private final String text;
        private int position;

        Parser(String text) {
            this.text = text;
        }

        boolean atEnd() {
            return position >= text.length();
        }

        void skipWhitespace() {
            while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
                position++;
            }
        }

        IllegalArgumentException error(String message) {
            return new IllegalArgumentException("Invalid JSON at position " + position + ": " + message);
        }

        Object readValue() {
            if (atEnd()) {
                throw error("Unexpected end of text");
            }
            char c = text.charAt(position);
            switch (c) {
                case '{':
                    return readObject();
                case '[':
                    return readArray();
                case '"':
                    return readString();
                case 't':
                    expectWord("true");
                    return Boolean.TRUE;
                case 'f':
                    expectWord("false");
                    return Boolean.FALSE;
                case 'n':
                    expectWord("null");
                    return null;
                default:
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        return readNumber();
                    }
                    throw error("Unexpected character '" + c + "'");
            }
        }

        private void expectWord(String word) {
            if (!text.startsWith(word, position)) {
                throw error("Expected '" + word + "'");
            }
            position += word.length();
        }

        private Map<String, Object> readObject() {
            Map<String, Object> object = new LinkedHashMap<>();
            position++; // {
            skipWhitespace();
            if (!atEnd() && text.charAt(position) == '}') {
                position++;
                return object;
            }
            while (true) {
                skipWhitespace();
                if (atEnd() || text.charAt(position) != '"') {
                    throw error("Expected a property name");
                }
                String key = readString();
                skipWhitespace();
                if (atEnd() || text.charAt(position) != ':') {
                    throw error("Expected ':'");
                }
                position++;
                skipWhitespace();
                object.put(key, readValue());
                skipWhitespace();
                if (atEnd()) {
                    throw error("Unexpected end of text inside an object");
                }
                char c = text.charAt(position++);
                if (c == '}') {
                    return object;
                }
                if (c != ',') {
                    position--;
                    throw error("Expected ',' or '}'");
                }
            }
        }

        private List<Object> readArray() {
            List<Object> array = new ArrayList<>();
            position++; // [
            skipWhitespace();
            if (!atEnd() && text.charAt(position) == ']') {
                position++;
                return array;
            }
            while (true) {
                skipWhitespace();
                array.add(readValue());
                skipWhitespace();
                if (atEnd()) {
                    throw error("Unexpected end of text inside an array");
                }
                char c = text.charAt(position++);
                if (c == ']') {
                    return array;
                }
                if (c != ',') {
                    position--;
                    throw error("Expected ',' or ']'");
                }
            }
        }

        private String readString() {
            StringBuilder value = new StringBuilder();
            position++; // opening quote
            while (true) {
                if (atEnd()) {
                    throw error("Unterminated string");
                }
                char c = text.charAt(position++);
                if (c == '"') {
                    return value.toString();
                }
                if (c != '\\') {
                    value.append(c);
                    continue;
                }
                if (atEnd()) {
                    throw error("Unterminated escape sequence");
                }
                char escaped = text.charAt(position++);
                switch (escaped) {
                    case '"':
                        value.append('"');
                        break;
                    case '\\':
                        value.append('\\');
                        break;
                    case '/':
                        value.append('/');
                        break;
                    case 'b':
                        value.append('\b');
                        break;
                    case 'f':
                        value.append('\f');
                        break;
                    case 'n':
                        value.append('\n');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    case 'u':
                        if (position + 4 > text.length()) {
                            throw error("Incomplete unicode escape");
                        }
                        try {
                            value.append((char) Integer.parseInt(text.substring(position, position + 4), 16));
                        } catch (NumberFormatException exception) {
                            throw error("Invalid unicode escape");
                        }
                        position += 4;
                        break;
                    default:
                        throw error("Invalid escape sequence '\\" + escaped + "'");
                }
            }
        }

        private Object readNumber() {
            int start = position;
            boolean decimal = false;
            if (text.charAt(position) == '-') {
                position++;
            }
            while (!atEnd()) {
                char c = text.charAt(position);
                if (c >= '0' && c <= '9') {
                    position++;
                } else if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') {
                    decimal = true;
                    position++;
                } else {
                    break;
                }
            }
            String number = text.substring(start, position);
            try {
                return decimal ? (Object) Double.valueOf(number) : (Object) Long.valueOf(number);
            } catch (NumberFormatException exception) {
                position = start;
                throw error("Invalid number '" + number + "'");
            }
        }
    }
}
