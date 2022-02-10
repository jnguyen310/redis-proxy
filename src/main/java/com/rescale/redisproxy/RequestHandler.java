package com.rescale.redisproxy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class RequestHandler implements HttpHandler {
    private static LRUCache cache;
    private static final int OK_RESPONSE  = 200;
    private static final int BAD_REQUEST_RESPONSE = 400;
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    public RequestHandler(LRUCache cache) {
        this.cache = cache;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String uri = exchange.getRequestURI().toString();
            int keyExists = uri.indexOf("key=");
            if (keyExists == -1) {
                LOGGER.error("The request does not contain a key parameter.");
                sendResponse(exchange, null, BAD_REQUEST_RESPONSE);
            }
            String key = uri.substring(uri.indexOf("key=") + 4);

            String value = cache.get(key);
            sendResponse(exchange, value, OK_RESPONSE);
        }
    }
    private void sendResponse(HttpExchange httpExchange, String requestParamValue, int responseCode) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        // this line is a must
        httpExchange.sendResponseHeaders(responseCode, requestParamValue.length());
        outputStream.write(requestParamValue.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}
