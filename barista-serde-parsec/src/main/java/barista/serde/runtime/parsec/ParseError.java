package barista.serde.runtime.parsec;

public record ParseError(int markIndex, int index, CharSequence seq, String message) {
    public String errorString() {
        int markAdjustment = 0;
        // we need to "adjust" markIndex if it lands on a line break or the end of the stream
        // (where "adjustment" causes the error column to refer to one column past the actual
        // line length)
        if ((markIndex < seq.length() && seq.charAt(markIndex) == '\n')
            || markIndex == seq.length()) {
            markAdjustment = 1;
        }
        int adjustedMarkIndex = Math.max(markIndex - markAdjustment, 0);

        int lineNumber = 1;
        int columnNumber = 1;
        int markStartLineCharIndex = 0;
        for (int i = 0; i < adjustedMarkIndex; i++) {
            if (seq.charAt(i) == '\n') {
                lineNumber++;
                columnNumber = 1;
                markStartLineCharIndex = i;
            } else {
                columnNumber++;
            }
        }

        int markEndLineCharIndex = seq.length();
        int errorColumnNumber = columnNumber;
        for (int i = adjustedMarkIndex; i < seq.length(); i++) {
            if (seq.charAt(i) == '\n') {
                markEndLineCharIndex = i;
                break;
            }
            if (i < index) {
                errorColumnNumber++;
            }
        }

        String context =
            seq.subSequence(markStartLineCharIndex, markEndLineCharIndex).toString();

        return "Parse error at line "
                + lineNumber
                + ", column "
                + columnNumber
                + ": "
                + message
                + ":\n"
                + context
                + "\n"
                + " ".repeat(columnNumber - 1)
                + "^"
                + "-".repeat(Math.max(errorColumnNumber - columnNumber - 2, 0))
                + "^".repeat(errorColumnNumber > columnNumber ? 1 : 0)
                + "\n";
    }
}
