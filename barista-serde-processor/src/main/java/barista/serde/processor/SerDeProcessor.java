package barista.serde.processor;

import barista.serde.annotations.SerDe;
import barista.serde.processor.JsonSerDeGenerator.JsonField;
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
import javax.lang.model.element.ElementKind;
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
                    String output = format(jf);
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

    /**
     * Formats the provided JavaFile, or, if the formatter throws an exception, returns the
     * JavaFile's default format.
     */
    private String format(JavaFile jf) {
        String javaFileAsString = jf.toString();
        try {
            return new Formatter(JavaFormatterOptions.builder().style(Style.AOSP).build())
                    .formatSourceAndFixImports(javaFileAsString);
        } catch (Exception e) {
            // if the formatter throws an exception, opt to output code anyway
            return javaFileAsString;
        }
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
            if (!ElementKind.RECORD.equals(classElement.getKind())) {
                error(
                        "SerDe.Json presently only generates serializers for record classes",
                        element);
            } else {
                List<? extends RecordComponentElement> recordComponents =
                        classElement.getRecordComponents();
                List<JsonField> fields =
                        recordComponents.stream()
                                .map(
                                        rce ->
                                                new JsonField(
                                                        rce.getSimpleName().toString(),
                                                        ClassName.get(rce.asType())))
                                .toList();
                filesFromRound.add(
                        JsonSerDeGenerator.generate(ClassName.get(classElement), fields));
            }
        }
        return filesFromRound;
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
