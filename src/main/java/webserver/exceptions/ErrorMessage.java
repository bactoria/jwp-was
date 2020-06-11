package webserver.exceptions;

import http.response.StatusCode;

public enum ErrorMessage {
    METHOD_NOT_ALLOWED(StatusCode.METHOD_NOT_ALLOWED, "지원하지 않는 Method 입니다. method: "),

    ILLEGAL_COOKIE_HEADER(StatusCode.INTERNAL_SERVER_ERROR, "유효하지 않은 쿠키값 입니다. cookie: "),
    ILLEGAL_PARAMETER(StatusCode.INTERNAL_SERVER_ERROR, "유효하지 않은 Parameters 입니다. parameter: "),
    UNSSUPORTED_ENCODING(StatusCode.INTERNAL_SERVER_ERROR, "지원하지 않는 Encoding 타입 입니다. encoder: "),
    PROCESSABLE_CONTROLLER_NOT_FOUND(StatusCode.INTERNAL_SERVER_ERROR, "해당 요청을 처리할 컨트롤러를 찾지 못했습니다. path:"),
    ;

    private final StatusCode statusCode;
    private final String message;

    ErrorMessage(StatusCode statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
