package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import barista.serde.runtime.parsec.Parsers;
import io.github.markelliot.result.Result;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Function;

final class AnyValueParser implements Parser<Object> {
    public static final Parser<Object> INSTANCE = new AnyValueParser();

    /** A null parser that always returns Optional.empty(). */
    private static final Parser<Optional<?>> EMPTY =
            Parsers.composeResult(JsonParsers.nullParser(), ignored -> Optional.empty());

    private AnyValueParser() {}

    @Override
    public Result<Object, ParseError> parse(ParseState state) {
        state.skipWhitespace();

        Parser<?> valueParser =
                switch (state.current()) {
                    case '{' -> JsonParsers.map(Function.identity(), this, LinkedHashMap::new);
                    case '[' -> JsonParsers.collection(this, ArrayList::new);
                    case '"' -> JsonParsers.string();
                    case 'n' -> EMPTY;
                    case 't', 'f' -> JsonParsers.booleanParser();
                    default -> JsonParsers.doubleParser();
                };

        return valueParser.parse(state).mapResult(r -> (Object) r);
    }
}
