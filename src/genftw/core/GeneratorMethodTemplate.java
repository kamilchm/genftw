package genftw.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Wraps a FreeMarker {@link Template}, allowing repeated template processing.
 */
public class GeneratorMethodTemplate {

    private final Filer filer;
    private final Template template;
    private final Map<String, Object> rootMap;

    public GeneratorMethodTemplate(Filer filer, Template template, Map<String, Object> rootMap) {
        this.filer = filer;
        this.template = template;
        this.rootMap = new HashMap<String, Object>(rootMap);
    }

    public void setRootModelMapping(String key, Object value) {
        rootMap.put(key, value);
    }

    public void process(Location outputRootLocation, String outputFile) throws IOException, TemplateException {
        PrintWriter outputWriter = null;
        try {
            // Create output writer
            FileObject resource = filer.createResource(outputRootLocation, "", outputFile);
            outputWriter = new PrintWriter(new BufferedWriter(resource.openWriter()));

            // Process template
            template.process(rootMap, outputWriter);
        } finally {
            // Close output writer
            if (outputWriter != null) {
                outputWriter.close();
            }
        }
    }

}
