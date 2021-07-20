package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import barista.serde.runtime.parsec.Parsers;
import io.github.markelliot.result.Result;

final class BooleanParser implements Parser<Boolean> {
    public static final Parser<Boolean> INSTANCE = new BooleanParser();

    private static final Parser<Boolean> TRUE =
            Parsers.composeResult(Parsers.expect("true"), ignored -> Boolean.TRUE);
    private static final Parser<Boolean> FALSE =
            Parsers.composeResult(Parsers.expect("false"), ignored -> Boolean.FALSE);

    private BooleanParser() {}

    @Override
    public Result<Boolean, ParseError> parse(ParseState state) {
        return switch (state.current()) {
            case 't' -> TRUE.parse(state);
            case 'f' -> FALSE.parse(state);
            default -> Result.error(state.mark().error("Expected value 'true' or 'false'"));
        };
    }
}
