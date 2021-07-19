package barista.serde.runtime.parsec;

import io.github.markelliot.result.Result;

final class WhitespaceParser implements Parser<Empty> {
    public static final Parser<Empty> INSTANCE = new WhitespaceParser();

    private WhitespaceParser() {}

    @Override
    public Result<Empty, ParseError> parse(ParseState state) {
        // throw away whitespace characters
        // we skip the isEndOfStream check because the EOS marker is not whitespace
        while (Character.isWhitespace(state.current())) {
            state.next();
        }
        return Result.ok(Empty.INSTANCE);
    }
}
