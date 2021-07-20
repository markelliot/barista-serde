# Barista SerDe
A compile-time SerDe generator and runtime support libraries.

Barista SerDe currently supports generating JSON SerDe classes for Java record classes labeled with
the `@SerDe.Json` annotation in projects with the `barista-serde-processor` annotation processor
configured. In this early-stage version, types must be Java primitives (`char`, `byte`, `short`,
`int`, `long`, `float`, `double`) or their boxed equivalents, `String`, `Optional`, `OptionalInt`, 
`OptionalLong`, `OptionalDouble`, `Collection`, `List`, or `Set`, or another record type that has 
been annotated with `@SerDe.Json` and with the annotation processor applied. Collection and Map
may contain any of those same types.

Generated SerDe classes will have the name `{RecordName}JsonSerDe` and will generate in the same
package as the originating type. These generated classes provide the following methods:
* `static JsonCharSeq serialize(RecordName)`: accepts a record and produces a JSON string
* `static Result<RecordName, ParseError> deserialize(JsonCharSeq)`: accepts a JSON string and
  produces a [`Result`](https://github.com/markelliot/result) containing a successfully 
  created `RecordName` or a `ParseError` indicating why it failed
* `static Parser<RecordName> parser()`: returns a `Parser` capable of parsing RecordName

This project aims to generate SerDe code that mimics what a human might author given the runtime
libraries, and to support an opinionated take on how to adapt Java objects to JSON strings. Despite
the opinionated stance, this library should be capable of serializing and deserializing any valid
JSON into a corresponding Java object.

In the near future, users might expect some additional functionality:
 - support for Java date/time types
 - aliases for record fields
 - support for 'alias' records (records containing a single field which should act as the value)
 - improved validation and error messaging on absence of required fields during deserialization
 - improved validation for map keys

Some of the libraries used in this project might be useful in other contexts, here's an overview:

#### barista-serde-parsec
A very basic [parser-combinator](https://en.wikipedia.org/wiki/Parser_combinator) library.

#### barista-serde-json
Runtime support for serializing objects to JSON and deserializing objects from JSON.

Serialization is mostly performed by [Serializers](/barista-serde-json/src/main/java/barista/serde/runtime/json/Serializers.java),
which provides adapters for Java's primitive types as well as Optionals, Collections, and Maps.

Deserializtion (or parsing) is mostly performed by [JsonParsers](/barista-serde-json/src/main/java/barista/serde/runtime/json/JsonParsers.java),
which provides adapters for Java's primitive types as well as Optionals, Collections, Maps and a
naive object-as-a-Map method.