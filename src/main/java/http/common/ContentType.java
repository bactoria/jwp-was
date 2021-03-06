package http.common;

public enum ContentType {
    TEXT_CSS_UTF_8("text/css;charset=utf-8"),
    TEXT_HTML_UTF_8("text/html;charset=utf-8"),
    TEXT_PLAIN_UTF_8("text/plain;charset=utf-8"),
    APPLICATION_X_WWW_FORM_UNLENCODED("application/x-www-form-urlencoded"),
    ;

    private String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
