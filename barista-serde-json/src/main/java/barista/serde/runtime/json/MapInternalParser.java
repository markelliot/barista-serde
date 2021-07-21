package barista.serde.runtime.json;

import barista.serde.runtime.parsec.Empty;
import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

final class MapInternalParser<K, V> implements Parser<Map<K, V>> {
    private final Function<String, K> keyFn;
    private final Function<K, Parser<V>> itemParser;
    private final Supplier<Map<K, V>> mapFactory;

    MapInternalParser(
            Function<String, K> keyFn,
            Function<K, Parser<V>> itemParser,
            Supplier<Map<K, V>> mapFactory) {
        this.keyFn = keyFn;
        this.itemParser = itemParser;
        this.mapFactory = mapFactory;
    }

    @Override
    public Result<Map<K, V>, ParseError> parse(ParseState state) {
        Map<K, V> map = mapFactory.get();
        while (!state.isEndOfStream()) {
            state.skipWhitespace();
            Result<K, ParseError> key = JsonParsers.string().parse(state).mapResult(keyFn);
            if (key.isError()) {
                return key.coerce();
            }

            Result<Empty, ParseError> sep = JsonParsers.keyValueSeparator().parse(state);
            if (sep.isError()) {
                return sep.coerce();
            }

            K realKey = key.unwrap();
            Result<V, ParseError> item = itemParser.apply(realKey).parse(state);
            if (item.isError()) {
                return item.coerce();
            }

            map.put(realKey, item.unwrap());

            // consume trailing whitespace
            state.skipWhitespace();
            if (state.current() == ',') {
                state.next(); // consume ','
            } else {
                break;
            }
        }
        return Result.ok(map);
    }
}
