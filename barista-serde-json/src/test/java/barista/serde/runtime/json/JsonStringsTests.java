package barista.serde.runtime.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

final class JsonStringsTests {
    @Test
    public void testUnescape() {
        assertThat(JsonStrings.unescape("").toString()).isEqualTo("");
        assertThat(JsonStrings.unescape("test").toString()).isEqualTo("test");
        assertThat(JsonStrings.unescape("\\\"").toString()).isEqualTo("\"");
        assertThat(JsonStrings.unescape("\\\\").toString()).isEqualTo("\\");
        assertThat(JsonStrings.unescape("\\/").toString()).isEqualTo("/");
        assertThat(JsonStrings.unescape("\\b").toString()).isEqualTo("\b");
        assertThat(JsonStrings.unescape("\\f").toString()).isEqualTo("\f");
        assertThat(JsonStrings.unescape("\\n").toString()).isEqualTo("\n");
        assertThat(JsonStrings.unescape("\\r").toString()).isEqualTo("\r");
        assertThat(JsonStrings.unescape("\\t").toString()).isEqualTo("\t");
        assertThat(JsonStrings.unescape("\\u0000").toString()).isEqualTo("\0");
        assertThat(JsonStrings.unescape("\\u0001").toString()).isEqualTo("\1");
    }

    @Test
    public void testUnescape_invalidEscapeSeq() {
        assertThat(JsonStrings.unescape("\\k").toString()).isEqualTo("\\k");
    }

    @Test
    void testEscape() {
        assertThat(JsonStrings.escape("")).isEqualTo("");
        assertThat(JsonStrings.escape("test")).isEqualTo("test");
        assertThat(JsonStrings.escape("\"")).isEqualTo("\\\"");
        assertThat(JsonStrings.escape("\\")).isEqualTo("\\\\");
        assertThat(JsonStrings.escape("/")).isEqualTo("\\/");
        assertThat(JsonStrings.escape("\b")).isEqualTo("\\b");
        assertThat(JsonStrings.escape("\f")).isEqualTo("\\f");
        assertThat(JsonStrings.escape("\n")).isEqualTo("\\n");
        assertThat(JsonStrings.escape("\r")).isEqualTo("\\r");
        assertThat(JsonStrings.escape("\t")).isEqualTo("\\t");
        assertThat(JsonStrings.escape("\0")).isEqualTo("\\u0000");
        assertThat(JsonStrings.escape("\1")).isEqualTo("\\u0001");

        assertThat(JsonStrings.escape("test\n")).isEqualTo("test\\n");
        assertThat(JsonStrings.escape("test\ntest")).isEqualTo("test\\ntest");
        assertThat(JsonStrings.escape("test\u0000test")).isEqualTo("test\\u0000test");
        assertThat(JsonStrings.escape("a\\b/c\"d")).isEqualTo("a\\\\b\\/c\\\"d");
    }
}
