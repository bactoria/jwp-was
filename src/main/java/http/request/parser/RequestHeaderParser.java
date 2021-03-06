package http.request.parser;

import http.common.HeaderField;
import http.request.RequestHeader;
import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.Map;

public class RequestHeaderParser {

    private static final String HEADER_NAME_VALUE_TOKENIZER = ":";

    public static RequestHeader parse(String headerLines) {
        final Map<String, HeaderField> header = new HashMap<>();

        String[] headersStr = headerLines.split("\n");
        for (String headerStr : headersStr) {
            if (Strings.isBlank(headerStr)) {
                continue;
            }

            final String[] h = headerStr.split(HEADER_NAME_VALUE_TOKENIZER,2);
            final String headerName = h[0].trim();
            final String headerValue = h[1].trim();

            final HeaderField headerField = new HeaderField(headerName, headerValue);
            header.put(headerName, headerField);
        }
        return new RequestHeader(header);
    }
}
