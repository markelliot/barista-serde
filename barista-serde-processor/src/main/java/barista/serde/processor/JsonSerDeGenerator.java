package barista.serde.processor;

import barista.serde.runtime.json.JsonCharSeq;
import barista.serde.runtime.json.JsonParsers;
import barista.serde.runtime.json.Serializers;
import barista.serde.runtime.parsec.ParseError;
import barista.serde.runtime.parsec.ParseState;
import barista.serde.runtime.parsec.Parser;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.github.markelliot.result.Result;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import javax.lang.model.element.Modifier;

public final class JsonSerDeGenerator {
    private static final String CLASS_EXT = "JsonSerDe";

    // TODO(markelliot): some options to consider in the future (in no particular order):
    //  - field name aliases
    //  - DateTimeFormatters for date-types (and support for date types)
    public record JsonField(String name, TypeName type) {}

    private JsonSerDeGenerator() {}

    // TODO(markelliot): some class-level options to consider in the future:
    //  - whether to throw or pass on unknown fields
    //  - how to handle missing field values (and what to do with nulls)
    //  - handle alias types
    //  - empty record types
    //  - whether to handle emitting optionals as nulls or to avoid emitting optionals at all
    //  - modulate behavior of floating point types with values +/-Inf and NaN
    // TODO(markelliot): some validations here or at the call-site for this method:
    //  - map keys are String-ish or integer-ish
    public static JavaFile generate(ClassName originalClass, List<JsonField> fields) {
        ClassName serDeClassName =
                ClassName.get(originalClass.packageName(), originalClass.simpleName() + CLASS_EXT);
        TypeSpec serializerClass =
                TypeSpec.classBuilder(serDeClassName.simpleName())
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(parserField(serDeClassName, originalClass, fields))
                        .addMethod(
                                MethodSpec.constructorBuilder()
                                        .addModifiers(Modifier.PRIVATE)
                                        .build())
                        .addMethod(serializer(originalClass, fields))
                        .addMethod(deserializer(originalClass))
                        .addMethod(parserMethod(originalClass))
                        .addMethod(mapper(originalClass, fields))
                        .build();
        return JavaFile.builder(originalClass.packageName(), serializerClass).build();
    }

    private static FieldSpec parserField(
            ClassName serDeClassName, ClassName originalClass, List<JsonField> fields) {
        TypeName parserType = ParameterizedTypeName.get(ClassName.get(Parser.class), originalClass);
        CodeBlock fieldParserAssignments =
                CodeBlock.join(
                        fields.stream()
                                .map(
                                        field ->
                                                CodeBlock.builder()
                                                        .addStatement(
                                                                "case $S -> $L",
                                                                field.name,
                                                                jsonParserCall(field.type))
                                                        .build())
                                .toList(),
                        "");

        return FieldSpec.builder(
                        parserType, "PARSER", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(
                        CodeBlock.builder()
                                .add("$T.object(field -> switch (field) {", JsonParsers.class)
                                .add(fieldParserAssignments)
                                .addStatement("default -> $T.any()", JsonParsers.class)
                                .add("},")
                                .add("$T::map", serDeClassName)
                                .add(")")
                                .build())
                .build();
    }

    private static CodeBlock jsonParserCall(TypeName type) {
        // primitives
        if (type.equals(ClassName.get(Boolean.class)) || type.equals(TypeName.BOOLEAN)) {
            return CodeBlock.of("$T.booleanParser()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(Character.class)) || type.equals(TypeName.CHAR)) {
            return CodeBlock.of("$T.charParser()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(Byte.class)) || type.equals(TypeName.BYTE)) {
            return CodeBlock.of("$T.byteParser()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(Short.class)) || type.equals(TypeName.SHORT)) {
            return CodeBlock.of("$T.shortParser()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(Integer.class)) || type.equals(TypeName.INT)) {
            return CodeBlock.of("$T.integerParser()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(Long.class)) || type.equals(TypeName.LONG)) {
            return CodeBlock.of("$T.longParser()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(Float.class)) || type.equals(TypeName.FLOAT)) {
            return CodeBlock.of("$T.floatParser()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(Double.class)) || type.equals(TypeName.DOUBLE)) {
            return CodeBlock.of("$T.doubleParser()", JsonParsers.class);
        }

        // non-primitive intrinsics
        if (type.equals(ClassName.get(String.class))) {
            return CodeBlock.of("$T.string()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(OptionalInt.class))) {
            return CodeBlock.of("$T.optionalInt()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(OptionalLong.class))) {
            return CodeBlock.of("$T.optionalLong()", JsonParsers.class);
        }
        if (type.equals(ClassName.get(OptionalDouble.class))) {
            return CodeBlock.of("$T.optionalDouble()", JsonParsers.class);
        }

        // parameterized intrinsics
        if (type instanceof ParameterizedTypeName parameterizedType) {
            ClassName rawType = parameterizedType.rawType;
            if (rawType.equals(ClassName.get(Optional.class))) {
                CodeBlock valueParser = jsonParserCall(parameterizedType.typeArguments.get(0));
                return CodeBlock.of("$T.optional($L)", JsonParsers.class, valueParser);
            }

            if (rawType.equals(ClassName.get(Collection.class))
                    || rawType.equals(ClassName.get(List.class))) {
                CodeBlock valueParser = jsonParserCall(parameterizedType.typeArguments.get(0));
                return CodeBlock.of(
                        "$T.collection($L, $T::new",
                        JsonParsers.class,
                        valueParser,
                        ArrayList.class);
            }

            if (rawType.equals(ClassName.get(Set.class))) {
                CodeBlock valueParser = jsonParserCall(parameterizedType.typeArguments.get(0));
                return CodeBlock.of(
                        "$T.collection($L, $T::new",
                        JsonParsers.class,
                        valueParser,
                        LinkedHashSet.class);
            }

            if (rawType.equals(ClassName.get(Map.class))) {
                CodeBlock keyParser = getKeyFn(parameterizedType.typeArguments.get(0));
                CodeBlock valueParser = jsonParserCall(parameterizedType.typeArguments.get(1));
                return CodeBlock.of(
                        "$T.map($L, $L, $T::new)",
                        JsonParsers.class,
                        keyParser,
                        valueParser,
                        LinkedHashMap.class);
            }
        }

        // TODO(markelliot): catch and warn on unsupported java.* types

        return useGeneratedParser(type);
    }

    private static CodeBlock useGeneratedParser(TypeName type) {
        if (!(type instanceof ClassName className)) {
            throw new IllegalStateException("Unexpected type: " + type.toString());
        }
        return CodeBlock.of(
                "$T.parser()",
                ClassName.get(className.packageName(), className.simpleName() + CLASS_EXT));
    }

    private static CodeBlock getKeyFn(TypeName type) {
        if (type.equals(ClassName.get(String.class))) {
            return CodeBlock.of("$T.identity()", Function.class);
        }
        return CodeBlock.of("$T::valueOf", type);
    }

    private static MethodSpec deserializer(ClassName originalClass) {
        return MethodSpec.methodBuilder("deserialize")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(JsonCharSeq.class, "json")
                .returns(
                        ParameterizedTypeName.get(
                                ClassName.get(Result.class),
                                originalClass,
                                ClassName.get(ParseError.class)))
                .addStatement("return PARSER.parse($T.of(json))", ParseState.class)
                .build();
    }

    private static MethodSpec mapper(ClassName originalClass, List<JsonField> fields) {
        CodeBlock args =
                CodeBlock.join(
                        fields.stream().map(JsonSerDeGenerator::getMapperCall).toList(), ",");
        return MethodSpec.methodBuilder("map")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(originalClass)
                .addParameter(
                        ParameterizedTypeName.get(Map.class, String.class, Object.class), "map")
                .addStatement("return new $T($L)", originalClass, args)
                .build();
    }

    private static CodeBlock getMapperCall(JsonField f) {
        if (f.type instanceof ParameterizedTypeName ptn
                && ptn.rawType.equals(ClassName.get(Optional.class))) {
            return CodeBlock.of(
                    "($T) map.getOrDefault($S, $T.empty())", f.type, f.name, Optional.class);
        }
        return CodeBlock.of("($T) map.get($S)", f.type, f.name);
    }

    private static MethodSpec parserMethod(ClassName originalClass) {
        return MethodSpec.methodBuilder("parser")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Parser.class), originalClass))
                .addStatement("return PARSER")
                .build();
    }

    private static MethodSpec serializer(ClassName originalClass, List<JsonField> fields) {
        CodeBlock fieldSerializers =
                fields.stream()
                        .map(JsonSerDeGenerator::generateFieldSerializationCode)
                        .collect(CodeBlock.joining(""));

        return MethodSpec.methodBuilder("serialize")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(originalClass, "value")
                .returns(JsonCharSeq.class)
                .addStatement(
                        "$T sj = new $T(\",\", \"{\", \"}\")",
                        StringJoiner.class,
                        StringJoiner.class)
                .addCode(fieldSerializers)
                .addStatement("return new $T(sj.toString())", JsonCharSeq.class)
                .build();
    }

    private static CodeBlock generateFieldSerializationCode(JsonField field) {
        boolean isNullable = !field.type.isPrimitive();

        CodeBlock serializerCode =
                serializerCall(field.type, CodeBlock.of("value.$N()", field.name), 0);

        CodeBlock.Builder cb = CodeBlock.builder();
        if (isNullable) {
            if ((field.type instanceof ParameterizedTypeName ptn
                            && ptn.rawType.equals(ClassName.get(Optional.class)))
                    || field.type.equals(ClassName.get(OptionalInt.class))
                    || field.type.equals(ClassName.get(OptionalLong.class))
                    || field.type.equals(ClassName.get(OptionalDouble.class))) {
                cb.beginControlFlow(
                        "if (value.$N() != null && value.$N().isPresent())",
                        field.name,
                        field.name);
            } else {
                cb.beginControlFlow("if (value.$N() != null)", field.name);
            }
        }
        cb.addStatement("sj.add($S + $L)", "\"" + field.name + "\":", serializerCode);
        if (isNullable) {
            cb.endControlFlow();
        }
        return cb.build();
    }

    private static final Set<TypeName> SIMPLE_INTRINSICS =
            ImmutableSet.of(
                    ClassName.get(String.class), ClassName.get(OptionalInt.class),
                    ClassName.get(OptionalLong.class), ClassName.get(OptionalDouble.class));

    private static CodeBlock serializerCall(
            TypeName type, CodeBlock fieldAccessor, int roundNumber) {
        // it's intrinsic but has no type parameters
        if (isSimpleIntrinsic(type)) {
            return CodeBlock.of("$T.serialize($L)", Serializers.class, fieldAccessor);
        }

        if (type instanceof ParameterizedTypeName parameterizedType) {
            // it's intrinsic and since we're here must have type parameters
            ClassName rawType = parameterizedType.rawType;
            if (isSingleParamIntrinsic(rawType)) {
                TypeName innerType = parameterizedType.typeArguments.get(0);
                return CodeBlock.of(
                        "$T.serialize($L, $N -> $L)",
                        Serializers.class,
                        fieldAccessor,
                        lambdaParam("v", roundNumber),
                        serializerCall(
                                innerType,
                                CodeBlock.of("$N", lambdaParam("v", roundNumber)),
                                roundNumber + 1));
            }

            if (isDualParamIntrinsic(rawType)) {
                TypeName keyType = parameterizedType.typeArguments.get(0);
                TypeName valueType = parameterizedType.typeArguments.get(1);
                return CodeBlock.of(
                        "$T.serialize($L, $N -> $L, $N -> $L)",
                        Serializers.class,
                        fieldAccessor,
                        lambdaParam("k", roundNumber),
                        serializerCall(
                                keyType,
                                CodeBlock.of("$N", lambdaParam("k", roundNumber)),
                                roundNumber + 1),
                        lambdaParam("v", roundNumber),
                        serializerCall(
                                valueType,
                                CodeBlock.of("$N", lambdaParam("v", roundNumber)),
                                roundNumber + 1));
            }
        }

        // is either not an intrinsic or at least not a supported intrinsic
        return useGeneratedSerializer(type, fieldAccessor);
    }

    private static String lambdaParam(String paramName, int roundNumber) {
        if (roundNumber == 0) {
            return paramName;
        }
        return paramName + roundNumber;
    }

    private static CodeBlock useGeneratedSerializer(TypeName type, CodeBlock fieldAccessor) {
        if (!(type instanceof ClassName className)) {
            throw new IllegalStateException("Unexpected type: " + type.toString());
        }
        return CodeBlock.of(
                "$T.serialize($L)",
                ClassName.get(className.packageName(), className.simpleName() + CLASS_EXT),
                fieldAccessor);
    }

    private static boolean isSimpleIntrinsic(TypeName type) {
        return type.isPrimitive() || type.isBoxedPrimitive() || SIMPLE_INTRINSICS.contains(type);
    }

    private static boolean isSingleParamIntrinsic(TypeName type) {
        return type.equals(ClassName.get(Optional.class))
                || type.equals(ClassName.get(Collection.class))
                || type.equals(ClassName.get(Set.class))
                || type.equals(ClassName.get(List.class));
    }

    private static boolean isDualParamIntrinsic(TypeName type) {
        return type.equals(ClassName.get(Map.class));
    }
}
