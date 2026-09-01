package com.github.jinzhu.now;

/**
 * Port of Go's {@code time.ParseError} plus the plain errors returned by
 * {@code now.Parse}. Go signals parse failures through a returned {@code error};
 * the Java port uses a checked exception so callers are forced to handle it the
 * same way.
 */
public class TimeParseException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String layout;
    private final String value;
    private final String layoutElem;
    private final String valueElem;

    /** Mirrors the plain {@code errors.New} failures raised by {@code parseWithFormat}. */
    public TimeParseException(String message) {
        super(message);
        this.layout = null;
        this.value = null;
        this.layoutElem = null;
        this.valueElem = null;
    }

    /** Mirrors {@code time.ParseError}. */
    public TimeParseException(String layout, String value, String layoutElem, String valueElem, String message) {
        super(buildMessage(layout, value, layoutElem, valueElem, message));
        this.layout = layout;
        this.value = value;
        this.layoutElem = layoutElem;
        this.valueElem = valueElem;
    }

    private static String buildMessage(String layout, String value, String layoutElem, String valueElem,
                                       String message) {
        if (message == null || message.isEmpty()) {
            return "parsing time \"" + value + "\" as \"" + layout + "\": cannot parse \"" + valueElem
                    + "\" as \"" + layoutElem + "\"";
        }
        return "parsing time \"" + value + "\"" + message;
    }

    public String layout() {
        return layout;
    }

    public String value() {
        return value;
    }

    public String layoutElem() {
        return layoutElem;
    }

    public String valueElem() {
        return valueElem;
    }
}
