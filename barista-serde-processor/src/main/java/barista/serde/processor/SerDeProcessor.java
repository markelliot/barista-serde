package barista.serde.processor;

import barista.serde.annotations.SerDe;
import com.google.auto.service.AutoService;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"barista.serde.annotations.SerDe.Json"})
public final class SerDeProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                // TODO(markelliot): render files
            } else {
                processImpl(annotations, roundEnv);
            }
        } catch (Exception e) {
            error("An error occurred during annotation processing: " + e.getMessage(), null);
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
            // TODO(markelliot): generate intermediate state
        }
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
