package barista.serde.runtime.json;

import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import io.github.markelliot.result.Result;
import java.util.Collection;
import java.util.function.Supplier;

final class CollectionInternalParser<T, C extends Collection<T>> implements Parser<C> {
    private final Parser<T> itemParser;
    private final Supplier<C> collectionFactory;

    CollectionInternalParser(Parser<T> itemParser, Supplier<C> collectionFactory) {
        this.itemParser = itemParser;
        this.collectionFactory = collectionFactory;
    }

    @Override
    public Result<C, ParseError> parse(ParseState state) {
        C collection = collectionFactory.get();
        while (!state.isEndOfStream()) {
            state.skipWhitespace();

            Result<T, ParseError> item = itemParser.parse(state);
            if (item.isError()) {
                return item.coerce();
            }
            item.mapResult(collection::add);

            state.skipWhitespace();
            if (state.current() == ',') {
                state.next(); // consume ','
            } else {
                break;
            }
        }
        return Result.ok(collection);
    }
}
