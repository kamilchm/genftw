package genftw.core;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

/**
 * Simple logging facade for JSR-269 {@link Messager}.
 */
public class ProcessorLogger {

    private final Messager messager;
    private final boolean verbose;

    public ProcessorLogger(Messager messager, boolean verbose) {
        this.messager = messager;
        this.verbose = verbose;
    }

    void log(Kind kind, String msg, Element elm) {
        if (elm != null) {
            messager.printMessage(kind, msg, elm);
        } else {
            messager.printMessage(kind, msg);
        }
    }

    public void info(String msg, Element elm) {
        if (verbose) {
            log(Kind.NOTE, msg, elm);
        }
    }

    public void info(String msg) {
        info(msg, null);
    }

    public void warning(String msg, Element elm) {
        log(Kind.WARNING, msg, elm);
    }

    public void warning(String msg) {
        warning(msg, null);
    }

    public void error(String msg, Exception ex, Element elm) {
        log(Kind.ERROR, formatErrorMessage(msg, ex), elm);
    }

    public void error(String msg, Exception ex) {
        error(msg, ex, null);
    }

    public void error(String msg, Element elm) {
        error(msg, null, elm);
    }

    public void error(String msg) {
        error(msg, null, null);
    }

    public String formatErrorMessage(String msg, Exception ex) {
        StringBuilder sb = new StringBuilder(msg);

        if (ex != null) {
            sb.append(": ").append(ex.getClass().getCanonicalName());
            sb.append(": ").append(ex.getLocalizedMessage());
        }

        return sb.toString();
    }

}
