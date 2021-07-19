package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import barista.serde.runtime.parsec.Parsers;
import io.github.markelliot.result.Result;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Function;

final class ValueConsumingParser implements Parser<Object> {
    public static final Parser<Object> INSTANCE = new ValueConsumingParser();

    private ValueConsumingParser() {}

    @Override
    public Result<Object, ParseError> parse(ParseState state) {
        Parsers.whitespace().parse(state);

        Parser<?> valueParser =
                switch (state.current()) {
                    case '"' -> JsonParsers.quotedString();
                    case '[' -> JsonParsers.collection(this, ArrayList::new);
                    case '{' -> JsonParsers.map(Function.identity(), this, LinkedHashMap::new);
                    default -> JsonParsers.doubleParser();
                };

        return valueParser.parse(state).mapResult(r -> (Object) r);
    }
}
