package http.request;

import http.common.Cookies;

public class HttpRequest {
    private final RequestLine requestLine;
    private final RequestHeader header;
    private final Cookies cookies;
    private final String body;

    public HttpRequest(RequestLine requestLine, RequestHeader header, Cookies cookies, String body) {
        this.requestLine = requestLine;
        this.header = header;
        this.cookies = cookies;
        this.body = body;
    }

    public HttpMethod getMethod() {
        return requestLine.getHttpMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getBody() {
        return body;
    }

    public String getCookie(String cookieName) {
        return cookies.getValue(cookieName);
    }
}
