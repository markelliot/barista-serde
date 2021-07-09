package barista.serde.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

public @interface SerDe {
    @Target({ElementType.TYPE})
    @interface Json {}
}
