package barista.serde.runtime.json;

import io.github.markelliot.result.Result;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JsonReader {
    private JsonReader() {}

    public static Result<? extends Object, Error> any(byte[] bytes) {
        return any(new State(bytes));
    }

    public static Result<? extends Object, Error> any(State state) {
        while (state.hasMore()) {
            if (isWhitespace(state.current())) {
                state.next();
            } else if (state.current() == '{') {
                return readObject(state);
            } else if (state.current() == '[') {
                return readArray(state);
            } else if (state.current() == '"') {
                return readString(state);
            } else if (state.current() == 't') {
                return readTrue(state);
            } else if (state.current() == 'f') {
                return readFalse(state);
            } else if (state.current() == 'n') {
                return readNull(state);
            } else {
                return readNumber(state);
            }
        }
        return Result.error(new Error());
    }

    private static Result<Map<String, Object>, Error> readObject(State state) {
        state.next(); // '{'
        Map<String, Object> map = new LinkedHashMap<>();
        while (state.hasMore()) {
            if (isWhitespace(state.current())) {
                // skip
            } else if (state.current() == '}') {
                // end of object
                break;
            } else {
                // read field
                Result<String, Error> maybeField = readString(state);
                if (maybeField.isError()) {
                    return maybeField.coerce();
                }
                String field = maybeField.unwrap();
                state.next();

                // skip some amount of whitespace
                whitespace(state);
                if (state.current() != ':') {
                    state.fail();
                    return Result.error(new Error());
                }
                state.next();

                // read value (value reader consumes whitespace)
                any(state).mapResult(value -> {
                    map.put(field, value);
                    return value;
                });
                state.next();

                whitespace(state);
                if (state.current() == '}') {
                    break;
                } else if (state.current() != ',') {
                    state.fail();
                    return Result.error(new Error());
                }
            }
            state.next();
        }
        return Result.ok(map);
    }

    private static void whitespace(State state) {
        while (state.hasMore() && isWhitespace(state.current())) {
            state.next();
        }
    }

    private static Result<List<Object>, Error> readArray(State state) {
        state.next(); // '['
        List<Object> items = new ArrayList<>();
        while(state.hasMore()) {
            if (isWhitespace(state.current()) || state.current() == ',') {
                // skip
            } else if (state.current() == ']') {
                // end of array
                break;
            } else {
                any(state).mapResult(items::add);
            }
            state.next();
        }
        if (!state.hasMore()) {
            state.fail();
            return Result.error(new Error());
        }
        return Result.ok(items);
    }

    private static Result<String, Error> readString(State state) {
        state.next(); // first quote
        int start = state.index;
        while (state.hasMore()) {
            if (state.current() == '"') {
                break;
            } else if (state.current() == '\\') {
                // skip the next byte, too
                state.next(2);
            } else {
                state.next();
            }
        }
        if (!state.hasMore()) {
            state.fail();
            return Result.error(new Error());
        }
        return Result.ok(new String(state.bytes, start, state.index - start, StandardCharsets.UTF_8));
    }

    private static Result<Boolean, Error> readTrue(State state) {
        if (state.has(3)
            && state.peekAt(1) == 'r'
            && state.peekAt(2) == 'u'
            && state.peekAt(3) == 'e') {
            state.next(3);
            return Result.ok(Boolean.FALSE);
        }
        state.fail();
        return Result.error(new Error());
    }

    private static Result<Boolean, Error> readFalse(State state) {
        if (state.has(4)
            && state.peekAt(1) == 'a'
            && state.peekAt(2) == 'l'
            && state.peekAt(3) == 's'
            && state.peekAt(4) == 'e') {
            state.next(4);
            return Result.ok(Boolean.FALSE);
        }
        state.fail();
        return Result.error(new Error());
    }

    private static Result<Optional<?>, Error> readNull(State state) {
        if (state.has(3)
            && state.peekAt(1) == 'u'
            && state.peekAt(2) == 'l'
            && state.peekAt(3) == 'l') {
            state.next(3);
            return Result.ok(Optional.empty());
        }
        state.fail();
        return Result.error(new Error());
    }

    private static Result<Number, Error> readNumber(State state) {
        int start = state.index;
        while (state.hasMore() && !isValueBoundary(state.current())) {
            state.next();
        }
        state.prev(); // walk backwards because we saw the boundary char
        try {
            return Result.ok(Double.parseDouble(new String(
                state.bytes, start,
                state.index - start + 1,
                StandardCharsets.UTF_8)));
        } catch (RuntimeException e) {
            return Result.error(new Error());
        }
    }

    private static boolean isValueBoundary(byte val) {
        return isWhitespace(val)
            || val == '[' || val == ']' || val == '{' || val == '}' || val == ',';
    }

    private static boolean isWhitespace(byte val) {
        return val == ' ' || val == '\n' || val == '\r' || val == '\t';
    }

    private static final class State {
        private final byte[] bytes;
        private int index;

        State(byte[] bytes) {
            this.bytes = bytes;
            this.index = 0;
        }

        boolean hasMore() {
            return index < bytes.length;
        }

        boolean has(int howManyMore) {
            return index + howManyMore < bytes.length;
        }

        byte current() {
            return bytes[index];
        }

        void prev() {
            index -= 1;
        }

        void next() {
            index += 1;
        }

        void next(int offset) {
            index += offset;
        }

        byte peekAt(int offset) {
            return bytes[index + offset];
        }

        void fail() {
            // set failure by moving index to the end
            this.index = bytes.length;
        }
    }

    public record Error() {}
}
