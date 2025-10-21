package barista.serde.runtime.parsec;

import com.markelliot.result.Result;

final class ExpectParser implements Parser<Character> {
    private final char expectation;

    ExpectParser(char expectation) {
        this.expectation = expectation;
    }

    @Override
    public Result<Character, ParseError> parse(ParseState state) {
        state.skipWhitespace();
        if (state.current() == expectation) {
            state.next(); // consume the expected char
            return Result.ok(expectation);
        }
        return Result.error(state.mark().error("Expected to find '" + expectation + "'"));
    }
}
