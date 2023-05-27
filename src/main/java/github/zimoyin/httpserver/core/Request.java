package github.zimoyin.httpserver.core;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import github.zimoyin.httpserver.SimpleConsoleFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * 请求
 */
public final class Request {
    private final HttpExchange exchange;
    private byte[] bytes = null;
    private final Logger logger = SimpleConsoleFormatter.installFormatter(Logger.getLogger(Response.class.getTypeName()));

    public Request(HttpExchange exchange) {
        this.exchange = exchange;
        parseGetParameters();

//        打印请求头
//        Headers headers = getHeaders();
//        if (headers.size() > 0)  logger.info("Request Headers Start");
//        else logger.info("Request Headers is empty");
//        headers.forEach((s, strings) -> logger.info(s + " : " + strings));
//        if (headers.size() > 0) logger.info("Request Headers End\n");
    }

    public String getMethod() {
        return exchange.getRequestMethod();
    }

    public String getPath(){
        return exchange.getRequestURI().getSchemeSpecificPart().trim();
    }

    public HttpExchange getExchange() {
        return exchange;
    }

    public Headers getHeaders() {
        return exchange.getRequestHeaders();
    }

    public URI getURI() {
        return exchange.getRequestURI();
    }

    /**
     * 获取 body 内容
     *
     * @return
     */
    public InputStream getBodyByInputStream() {
        return exchange.getRequestBody();
    }

    public byte[] getBody() throws IOException {
        InputStream stream = getBodyByInputStream();
        if (bytes == null) bytes =stream.readAllBytes();
        return bytes;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //TODO: 只能解析GET的参数，无法解析 x-www-form-urlencoded 的参数，请重新设计，但是要注意不要关闭 body 流
    private final HashMap<String, String> parameters = new HashMap<String, String>();
    /**
     * 服务器解析请求用的字符集
     */
    private Charset charset = StandardCharsets.UTF_8;

    /**
     * 解析GET的参数
     */
    private void parseGetParameters() {
        String query = getURI().getQuery();
        if (!getMethod().equalsIgnoreCase("GET")) return;
        parseUrlParameters(query);
    }

    /**
     * Post 参数类型解析: x-www-form-urlencoded
     * @param body 参数体
     */
    @Deprecated
    private void parseUrlencodedParameters(String body) throws IOException {
        parseUrlParameters(body);
    }

    /**
     * 通用URL参数解析
     *
     * @param body 参数列表
     */
    private void parseUrlParameters(String body) {
        if (body == null || body.isEmpty()) return;
        for (String str : body.split("&")) {
            if (str == null || str.isEmpty()) continue;
            String[] vars = str.split("=", 2);
            String key;
            String value = null;
            if (vars.length >= 2) {
                key = vars[0];
                value = vars[1];
            } else {
                key = vars[0];
            }
            parameters.put(key, value);
        }
    }


    public String getParameter(String key) {
        return parameters.get(key);
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
