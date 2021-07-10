package barista.serde.processor;

import barista.serde.runtime.JsonCharSeq;
import barista.serde.runtime.Serializers;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.StringJoiner;
import javax.lang.model.element.Modifier;

public final class JsonSerializerGenerator {

    public record JsonField(String name, TypeName type) {}

    private JsonSerializerGenerator() {}

    public static JavaFile generate(ClassName originalClass, List<JsonField> fields) {
        CodeBlock fieldSerializers =
                fields.stream()
                        .map(JsonSerializerGenerator::generateFieldSerializationCode)
                        .collect(CodeBlock.joining(""));

        TypeSpec serializerClass =
                TypeSpec.classBuilder(originalClass.simpleName() + "JsonSerializer")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(
                                MethodSpec.constructorBuilder()
                                        .addModifiers(Modifier.PRIVATE)
                                        .build())
                        .addMethod(
                                MethodSpec.methodBuilder("serialize")
                                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                        .addParameter(originalClass, "value")
                                        .returns(JsonCharSeq.class)
                                        .addStatement(
                                                "$T sj = new $T(\",\", \"{\", \"}\")",
                                                StringJoiner.class,
                                                StringJoiner.class)
                                        .addCode(fieldSerializers)
                                        .addStatement(
                                                "return new $T(sj.toString())", JsonCharSeq.class)
                                        .build())
                        .build();
        return JavaFile.builder(originalClass.packageName(), serializerClass).build();
    }

    private static CodeBlock generateFieldSerializationCode(JsonField field) {
        boolean isNullable = !field.type.isPrimitive();

        CodeBlock serializerCode =
                serializerCall(field.type, CodeBlock.of("value.$N()", field.name), 0);

        CodeBlock.Builder cb = CodeBlock.builder();
        if (isNullable) {
            cb.beginControlFlow("if (value.$N() != null)", field.name);
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
                ClassName.get(className.packageName(), className.simpleName() + "JsonSerializer"),
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
