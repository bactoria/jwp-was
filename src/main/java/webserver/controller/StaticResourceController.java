package webserver.controller;

import http.common.ContentType;
import http.request.HttpRequest;
import http.response.HttpResponse;
import utils.FileIoUtils;

public class StaticResourceController implements Controller {

    private StaticResourceController() {
    }

    private static class Singleton {
        private static final StaticResourceController instance = new StaticResourceController();
    }

    public static StaticResourceController getInstance() {
        return StaticResourceController.Singleton.instance;
    }

    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) {
        final String path = httpRequest.getPath();
        final byte[] stylesheet = FileIoUtils.loadFileFromClasspath("./static/" + path);

        httpResponse.setContentType(ContentType.TEXT_CSS_UTF_8);
        httpResponse.setBody(stylesheet);
    }
}
