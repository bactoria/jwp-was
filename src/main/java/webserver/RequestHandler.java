package webserver;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import db.DataBase;
import http.*;
import http.parser.RequestHeaderParser;
import http.parser.RequestLineParser;
import http.request.HttpRequest;
import http.request.RequestLine;
import http.response.HttpResponse;
import model.User;
import model.Users;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.FileIoUtils;
import utils.HandlebarsHelper;
import utils.IOUtils;

import java.io.*;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest httpRequest = readRequest(in);

            String path = httpRequest.getPath();

            DataOutputStream dos = new DataOutputStream(out);
            HttpResponse httpResponse = new HttpResponse();

            if (isPost(httpRequest) && "/user/create".equals(path)) {
                FormData formData = new FormData(httpRequest.getBody());
                String userId = formData.getValue("userId");
                String password = formData.getValue("password");
                String name = formData.getValue("name");
                String email = formData.getValue("email");

                DataBase.addUser(new User(userId, password, name, email));

                httpResponse.response302("/index.html");
            } else if (isPost(httpRequest) && "/user/login".equals(path)) {
                FormData formData = new FormData(httpRequest.getBody());
                String userId = formData.getValue("userId");
                String password = formData.getValue("password");

                User user = DataBase.findUserById(userId);

                if (isSamePassword(user.getPassword(), password)) {
                    httpResponse.response302("/index.html");
                    httpResponse.addCookie("logined", "true");
                    httpResponse.addCookiePath("/");
                } else {
                    httpResponse.response302("/user/login_failed.html");
                    httpResponse.addCookie("logined", "false");
                    httpResponse.addCookiePath("/");
                }
            } else if (isGet(httpRequest) && "/user/list".equals(path)) {
                if (isLogined(httpRequest)) {
                    Users users = new Users(DataBase.findAll());

                    TemplateLoader loader = new ClassPathTemplateLoader();
                    loader.setPrefix("/templates");
                    loader.setSuffix(".html");

                    Handlebars handlebars = new Handlebars(loader);
                    handlebars.registerHelpers(new HandlebarsHelper());

                    Template template = handlebars.compile("user/list");

                    byte[] htmlFile = template.apply(users).getBytes();
                    httpResponse.response200HTML(htmlFile);
                } else {
                    httpResponse.response302("/index.html");
                }
            } else {
                if (path.endsWith(".css")) {
                    byte[] body = FileIoUtils.loadFileFromClasspath("./static/" + path);
                    response200StylesheetHeader(dos, body.length);
                    responseBody(dos, body);
                } else {
                    byte[] html = FileIoUtils.loadFileFromClasspath("./templates/" + path);
                    httpResponse.response200HTML(html);
                }
            }

            writeResponse(dos, httpResponse);

        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
        }
    }

    private void writeResponse(DataOutputStream dos, HttpResponse httpResponse) {
        try {
            writeResponseLine(dos, httpResponse);
            writeCookie(dos, httpResponse);
            writeHeader(dos, httpResponse);
            writeResponseBody(dos, httpResponse);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void writeResponseLine(DataOutputStream dos, HttpResponse httpResponse) throws IOException {
        dos.writeBytes("HTTP/1.1 " + httpResponse.getStatusCode() + " FOUND \r\n");
    }

    private void writeHeader(DataOutputStream dos, HttpResponse httpResponse) throws IOException {
        for (Iterator it = httpResponse.getHeaders().iterator(); it.hasNext(); ) {
            String headerName = (String) it.next();
            String headerValue = httpResponse.getHeader(headerName);
            dos.writeBytes(headerName + ": " + headerValue + "\r\n");
        }
        dos.writeBytes("\r\n");
    }

    private void writeCookie(DataOutputStream dos, HttpResponse httpResponse) throws IOException {
        Cookies cookies = httpResponse.getCookie();
        if (!cookies.isEmpty()) {
            dos.writeBytes("Set-Cookie: " + cookies.stringify() + "\r\n");
        }
    }

    private void writeResponseBody(DataOutputStream dos, HttpResponse httpResponse) throws IOException {
        dos.write(httpResponse.getBody(), 0, httpResponse.getBody().length);
    }

    private HttpRequest readRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        String requestLineStr = br.readLine();
        logger.debug("Request Line :: {}", requestLineStr);
        RequestLine requestLine = RequestLineParser.parse(requestLineStr);

        List<String> headerList = new ArrayList<>();
        String headerStr = br.readLine();
        while (!headerStr.equals("")) {
            logger.debug("Header :: {}", headerStr);
            headerList.add(headerStr);
            headerStr = br.readLine();
        }
        Headers headers = RequestHeaderParser.parse(headerList);

        String contentLengthStr = headers.getValue("Content-Length");
        int contentLength = 0;
        if (!Strings.isBlank(contentLengthStr)) {
            contentLength = Integer.parseInt(contentLengthStr);
        }

        String requestBody = IOUtils.readData(br, contentLength);
        logger.debug("Body :: {}", requestBody);

        return new HttpRequest(requestLine, headers, requestBody);

    }

    private boolean isSamePassword(String password1, String password2) {
        if (password1 == null) {
            return false;
        }
        return password1.equals(password2);
    }

    private boolean isLogined(HttpRequest httpRequest) {
        String logined = httpRequest.getCookie("logined");
        if ("true".equals(logined)) {
           return true;
        }
        return false;
    }

    private boolean isGet(HttpRequest httpRequest) {
        return HttpMethod.GET.equals(httpRequest.getMethod());
    }

    private boolean isPost(HttpRequest httpRequest) {
        return HttpMethod.POST.equals(httpRequest.getMethod());
    }

    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response200HtmlHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response200StylesheetHeader(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void response302HeaderWithCookies(DataOutputStream dos, String location, String cookieName, String cookieValue) {
        try {
            dos.writeBytes("HTTP/1.1 302 FOUND \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("Set-Cookie: " + cookieName + "=" + cookieValue + "; Path=/\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
