package barista.serde.runtime;

public final class JsonStrings {
    private JsonStrings() {}

    public static CharSequence unescape(CharSequence value) {
        // we could potentially defer the creation of this StringBuilder
        // until we need it but it should be light-weight-ish?
        StringBuilder sb = new StringBuilder();
        int read = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\\') {
                i += 1;
                char next = value.charAt(i);
                switch (next) {
                    case '"', '\\', '/' -> {
                        sb.append(value, read, i - 1).append(next);
                        i += 1;
                    }
                    case 'b' -> {
                        sb.append(value, read, i - 1).append('\b');
                        i += 1;
                    }
                    case 'f' -> {
                        sb.append(value, read, i - 1).append('\f');
                        i += 1;
                    }
                    case 'n' -> {
                        sb.append(value, read, i - 1).append('\n');
                        i += 1;
                    }
                    case 'r' -> {
                        sb.append(value, read, i - 1).append('\r');
                        i += 1;
                    }
                    case 't' -> {
                        sb.append(value, read, i - 1).append('\t');
                        i += 1;
                    }
                    case 'u' -> {
                        sb.append(value, read, i - 1)
                                .append(
                                        Character.toString(
                                                Integer.valueOf(
                                                        value.subSequence(i + 1, i + 5).toString(),
                                                        16)));
                        i += 5;
                    }
                    default -> {
                        // if we get here the escaped character wasn't a valid escape
                        // TODO(markelliot): we should log something here
                        sb.append(value, read, i + 1);
                        i += 1;
                    }
                }
                read = i;
            }
        }
        // short-circuit if we made no replacements
        if (read == 0) {
            return value;
        }
        // we made some replacements, so add the remainder
        sb.append(value, read, value.length());
        // TODO(markelliot): StringBuilder#toString vs. CharSequence?
        return sb.toString();
    }

    // TODO(markelliot): fix this to take a CharSequence and adjust foreach loop to for loop
    public static CharSequence escape(String value) {
        StringBuilder sb = new StringBuilder();
        int written = 0;
        int index = 0;

        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '"':
                case '\\':
                case '/':
                    sb.append(value, written, index).append("\\").append(ch);
                    written = index + 1;
                    break;
                case '\b':
                    sb.append(value, written, index).append("\\b");
                    written = index + 1;
                    break;
                case '\f':
                    sb.append(value, written, index).append("\\f");
                    written = index + 1;
                    break;
                case '\n':
                    sb.append(value, written, index).append("\\n");
                    written = index + 1;
                    break;
                case '\r':
                    sb.append(value, written, index).append("\\r");
                    written = index + 1;
                    break;
                case '\t':
                    sb.append(value, written, index).append("\\t");
                    written = index + 1;
                    break;
                default:
                    if (ch < 0x20) {
                        String charCode = Integer.toHexString(ch);
                        sb.append(value, written, index)
                                .append("\\u")
                                .append("0".repeat(4 - charCode.length()))
                                .append(charCode);
                        written = index + 1;
                    }
                    break;
            }
            index++;
        }
        // add remainder
        sb.append(value, written, value.length());
        // TODO(markelliot): is it better to return the StringBuilder (which is a CharSequence) or
        //  the rendered String here?
        return sb.toString();
    }
}
