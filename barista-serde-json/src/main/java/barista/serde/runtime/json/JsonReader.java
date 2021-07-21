package barista.serde.runtime.json;

import java.nio.charset.StandardCharsets;

public final class JsonReader {
    private JsonReader() {}

    public static int any(byte[] bytes) {
        int last = any(bytes, 0);
        // System.out.println("Finished parsing at " + last + " of " + (bytes.length - 1));
        return last;
    }

    public static int any(byte[] bytes, int oldIndex) {
        int index = oldIndex;
        while (index < bytes.length) {
            if (isWhitespace(bytes[index])) {
                // whitespace
                index += 1;
            } else if (bytes[index] == '{') {
                // start obj
                // System.out.println("start obj " + index);
                index = readObject(bytes, index);
                // System.out.println("end obj " + index);
                return index;
            } else if (bytes[index] == '[') {
                // start array
                // System.out.println("start arr " + index);
                index = readArray(bytes, index);
                // System.out.println("end   arr " + index);
                return index;
            } else if (bytes[index] == '"') {
                // start string
                // System.out.println("start str " + index);
                index = readString(bytes, index);
                // System.out.println("end   str " + index);
                return index;
            } else if (bytes[index] == 't') {
                // true
                // System.out.println("start tru " + index);
                index = readTrue(bytes, index);
                // System.out.println("end   tru " + index);
                return index;
            } else if (bytes[index] == 'f') {
                // false
                // System.out.println("start fal " + index);
                index = readFalse(bytes, index);
                // System.out.println("end   fal " + index);
                return index;
            } else if (bytes[index] == 'n') {
                // null
                // System.out.println("start nul " + index);
                index = readNull(bytes, index);
                // System.out.println("end   nul " + index);
                return index;
            } else {
                // try it as a number
                // System.out.println("start num " + index);
                index = readNumber(bytes, index);
                // System.out.println("end   num " + index);
                return index;
            }
        }
        return index;
    }

    private static int readObject(byte[] bytes, int index) {
        int newIndex = index + 1;
        while (newIndex < bytes.length) {
            if (isWhitespace(bytes[newIndex])) {
                // skip
            } else if (bytes[newIndex] == '}') {
                // end of object
                break;
            } else {
                // read field
                // System.out.println("  obj field start " + newIndex);
                newIndex = readString(bytes, newIndex) + 1;
                // System.out.println("  obj field end   " + newIndex);
                // skip some amount of whitespace
                newIndex = whitespace(bytes, newIndex);
                if (bytes[newIndex] != ':') {
                    // System.out.println("expected ':' but didn't find one");
                    // fail
                    return bytes.length;
                }

                // read value (value reader consumes whitespace)
                newIndex = any(bytes, newIndex + 1) + 1;

                newIndex = whitespace(bytes, newIndex);
                if (bytes[newIndex] == '}') {
                    break;
                } else if (bytes[newIndex] != ',') {
                    // System.out.println("expected ',' but didn't find either");
                    return bytes.length;
                }
            }
            newIndex += 1;
        }
        return newIndex;
    }

    private static int whitespace(byte[] bytes, int oldIndex) {
        int index = oldIndex;
        while (index < bytes.length && isWhitespace(bytes[index])) {
            index += 1;
        }
        return index;
    }

    private static int readArray(byte[] bytes, int index) {
        int newIndex = index + 1;
        while(newIndex < bytes.length) {
            if (isWhitespace(bytes[newIndex]) || bytes[newIndex] == ',') {
                // skip
            } else if (bytes[newIndex] == ']') {
                // end of array
                break;
            } else {
                // System.out.println("  array entry " + arrayEntry);
                newIndex = any(bytes, newIndex);
            }
            newIndex += 1;
        }
        return newIndex;
    }

    private static int readString(byte[] bytes, int index) {
        int newIndex = index + 1;
        while (newIndex < bytes.length) {
            if (bytes[newIndex] == '"') {
                break;
            } else if (bytes[newIndex] == '\\') {
                // skip the next byte, too
                newIndex += 2;
            } else {
                newIndex += 1;
            }
        }
        // System.out.println(new String(bytes, index, newIndex - index + 1, StandardCharsets.UTF_8));
        return newIndex;
    }

    private static int readTrue(byte[] bytes, int index) {
        if (index + 3 < bytes.length
            && bytes[index + 1] == 'r'
            && bytes[index + 2] == 'u'
            && bytes[index + 3] == 'e') {
            return index + 3;
        }
        return bytes.length;
    }

    private static int readFalse(byte[] bytes, int index) {
        if (index + 4 < bytes.length
            && bytes[index + 1] == 'a'
            && bytes[index + 2] == 'l'
            && bytes[index + 3] == 's'
            && bytes[index + 4] == 'e') {
            return index + 4;
        }
        return bytes.length;
    }

    private static int readNull(byte[] bytes, int index) {
        if (index + 3 < bytes.length
            && bytes[index + 1] == 'u'
            && bytes[index + 2] == 'l'
            && bytes[index + 3] == 'l') {
            return index + 3;
        }
        return bytes.length;
    }

    private static int readNumber(byte[] bytes, int index) {
        int newIndex = index;
        while (newIndex < bytes.length && !isValueBoundary(bytes[newIndex])) {
            newIndex += 1;
        }
        return newIndex - 1;
    }

    private static boolean isValueBoundary(byte val) {
        return isWhitespace(val)
            || val == '[' || val == ']' || val == '{' || val == '}' || val == ',';
    }

    private static boolean isWhitespace(byte val) {
        return val == ' ' || val == '\n' || val == '\r' || val == '\t';
    }
}
