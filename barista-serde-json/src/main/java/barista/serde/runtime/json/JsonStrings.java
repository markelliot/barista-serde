package barista.serde.runtime.json;

public final class JsonStrings {
    private JsonStrings() {}

    public static CharSequence unescape(CharSequence value) {
        // we could potentially defer the creation of this StringBuilder
        // until we need it but it should be light-weight-ish?
        StringBuilder sb = new StringBuilder();
        int read = 0;
        for (int i = 0; i < value.length(); i++) {
            char next = value.charAt(i);
            if (next == '\\') {
                i += 1;
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

    public static CharSequence escape(CharSequence value) {
        StringBuilder sb = new StringBuilder();
        int written = 0;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"', '\\', '/' -> {
                    sb.append(value, written, i).append("\\").append(ch);
                    written = i + 1;
                }
                case '\b' -> {
                    sb.append(value, written, i).append("\\b");
                    written = i + 1;
                }
                case '\f' -> {
                    sb.append(value, written, i).append("\\f");
                    written = i + 1;
                }
                case '\n' -> {
                    sb.append(value, written, i).append("\\n");
                    written = i + 1;
                }
                case '\r' -> {
                    sb.append(value, written, i).append("\\r");
                    written = i + 1;
                }
                case '\t' -> {
                    sb.append(value, written, i).append("\\t");
                    written = i + 1;
                }
                default -> {
                    if (ch < 0x20) {
                        String charCode = Integer.toHexString(ch);
                        sb.append(value, written, i)
                                .append("\\u")
                                .append("0".repeat(4 - charCode.length()))
                                .append(charCode);
                        written = i + 1;
                    }
                }
            }
        }
        // short-circuit if we made no replacements
        if (written == 0) {
            return value;
        }
        // add remainder
        sb.append(value, written, value.length());
        // TODO(markelliot): StringBuilder#toString vs. CharSequence?
        return sb.toString();
    }
}
