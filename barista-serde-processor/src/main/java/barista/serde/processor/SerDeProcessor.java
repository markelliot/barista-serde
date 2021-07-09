package barista.serde.processor;

import barista.serde.annotations.SerDe;
import barista.serde.processor.JsonSerializerGenerator.JsonField;
import com.google.auto.service.AutoService;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.google.googlejavaformat.java.JavaFormatterOptions.Style;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"barista.serde.annotations.SerDe.Json"})
@SupportedSourceVersion(SourceVersion.RELEASE_16)
public final class SerDeProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (!roundEnv.processingOver()) {
                Set<JavaFile> filesFromRound = processImpl(annotations, roundEnv);
                for (JavaFile jf : filesFromRound) {
                    String output =
                            new Formatter(JavaFormatterOptions.builder().style(Style.AOSP).build())
                                    .formatSourceAndFixImports(jf.toString());
                    writeTo(jf.packageName, jf.typeSpec, output, processingEnv.getFiler());
                }
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            error("An error occurred during annotation processing: " + sw, null);
        }
        return false;
    }

    // Copied from JavaFile
    public void writeTo(String packageName, TypeSpec typeSpec, String formattedSource, Filer filer)
            throws IOException {
        String fileName = packageName.isEmpty() ? typeSpec.name : packageName + "." + typeSpec.name;
        List<Element> originatingElements = typeSpec.originatingElements;
        JavaFileObject filerSourceFile =
                filer.createSourceFile(fileName, originatingElements.toArray(new Element[0]));
        try (Writer writer = filerSourceFile.openWriter()) {
            writer.write(formattedSource);
        } catch (Exception e) {
            try {
                filerSourceFile.delete();
            } catch (Exception ignored) {
            }
            throw e;
        }
    }

    public Set<JavaFile> processImpl(
            Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<JavaFile> filesFromRound = new LinkedHashSet<>();

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
            } else {
                List<JsonField> fields =
                        recordComponents.stream()
                                .map(
                                        rce ->
                                                new JsonField(
                                                        rce.getSimpleName().toString(),
                                                        ClassName.get(rce.asType())))
                                .toList();
                filesFromRound.add(
                        JsonSerializerGenerator.generate(ClassName.get(classElement), fields));
            }
        }
        return filesFromRound;
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
