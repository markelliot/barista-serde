package barista.serde.runtime.parsec;

public record ParseError(
        int lineNumber, int columnNumber, int endColumnNumber, String context, String message) {

    @Override
    public String toString() {
        return errorString();
    }

    public String errorString() {
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
                + "-".repeat(Math.max(endColumnNumber - columnNumber - 2, 0))
                + "^".repeat(endColumnNumber > columnNumber ? 1 : 0)
                + "\n";
    }
}
