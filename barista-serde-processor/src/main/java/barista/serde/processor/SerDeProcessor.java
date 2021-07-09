package barista.serde.processor;

import barista.serde.annotations.SerDe;
import barista.serde.processor.JsonSerializerGenerator.JsonField;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"barista.serde.annotations.SerDe.Json"})
@SupportedSourceVersion(SourceVersion.RELEASE_16)
public final class SerDeProcessor extends AbstractProcessor {
    private final Set<JavaFile> newFiles = new LinkedHashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                for (JavaFile jf : newFiles) {
                    jf.writeTo(processingEnv.getFiler());
                }
            } else {
                processImpl(annotations, roundEnv);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            error("An error occurred during annotation processing: " + sw, null);
        }
        return false;
    }

    public void processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SerDe.Json.class)) {
            if (!element.getKind().isClass()) {
                error("SerDe.Json is only applicable for classes", element);
                continue;
            }

            TypeElement classElement = (TypeElement) element;
            List<? extends RecordComponentElement> recordComponents =
                    classElement.getRecordComponents();
            if (recordComponents.isEmpty()) {
                error(
                        "SerDe.Json presently only generates serializers for record classes",
                        element);
                continue;
            } else {
                List<JsonField> fields =
                        recordComponents.stream()
                                .map(
                                        rce ->
                                                new JsonField(
                                                        rce.getSimpleName().toString(),
                                                        ClassName.get(rce.asType())))
                                .toList();
                newFiles.add(JsonSerializerGenerator.generate(ClassName.get(classElement), fields));
            }
        }
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
