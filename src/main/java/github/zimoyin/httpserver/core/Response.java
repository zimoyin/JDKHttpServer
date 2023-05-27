package github.zimoyin.httpserver.core;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 响应
 */
public final class Response {
    private final HttpExchange exchange;
    private final ByteArrayOutputStream ByteArray = new ByteArrayOutputStream();
    private boolean isEventSource = false;
    private int code = 200;
    private boolean closed = false;

    public Response(HttpExchange exchange) {
        this.exchange = exchange;
        getHeaders().set("Content-Type", "text/html; charset=utf-8");
    }

    private OutputStream getResponseBody() throws IOException {
        return exchange.getResponseBody();
    }

    public void write(int body) throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");

        ByteArray.write(body);
    }

    public void write(byte[] body) throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");

        ByteArray.write(body);
    }

    public void write(byte[] body, int off, int len) throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");

        ByteArray.write(body, off, len);
    }

    public void write(String body) throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");

        ByteArray.write(body.getBytes(StandardCharsets.UTF_8));
    }

    public void write(String body, Charset charset) throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");

        ByteArray.write(body.getBytes(charset));
    }

    public void flush() throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");
        ByteArray.flush();
        getResponseBody().flush();
    }

    /**
     * 发送信息，并刷新流。如果该响应是 EventSource 则发送单次事件。注意该发送方法与write/end 是分离的
     */
    @Deprecated
    public void send(byte[] body) throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");
        if (!isEventSource) throw new IOException("Response stream not is a EventSource");
        getResponseBody().write(body);
        getResponseBody().flush();
    }

    /**
     * 发送信息，并刷新流。如果该响应是 EventSource 则发送单次事件。
     * 注意该发送方法与write/end 是分离的，请不要使用write 为 该方法写入数据该方法不会发送的
     */
    @Deprecated
    public void send(byte[] body, int off, int len) throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");
        if (!isEventSource) throw new IOException("Response stream not is a EventSource");
        getResponseBody().write(body, off, len);
        getResponseBody().flush();
    }

    /**
     * 发送信息，并刷新流。如果该响应是 EventSource 则发送单次事件。
     * 注意该发送方法与write/end 是分离的，请不要使用write 为 该方法写入数据该方法不会发送的
     */
    public void send(String body, Charset charset) throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");
        if (!isEventSource) throw new IOException("Response stream not is a EventSource");
        getResponseBody().write(("data:" + body + "\n\n").getBytes(charset));
        getResponseBody().flush();
    }

    /**
     * 发送信息，并刷新流。如果该响应是 EventSource 则发送单次事件。
     * 注意该发送方法与write/end 是分离的，请不要使用write 为 该方法写入数据该方法不会发送的
     */
    public void send(String body) throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");
        if (!isEventSource) throw new IOException("Response stream not is a EventSource");
        PrintWriter out = new PrintWriter(getResponseBody());
        out.write("data:" + body + "\n\n");
        out.flush();
    }


    /**
     * 设置服务器为事件源
     */
    public void setEventSource() throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");
        if (isEventSource) throw new IOException("Response EventSource is set repeatedly");
        getHeaders().set("Content-Type", "text/event-stream");
        getHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(code, 0);
        isEventSource = true;
    }

    /**
     * 启用跨域支持
     */
    public void setCrossDomain() {
        if (isEventSource || isClosed())
            try {
                throw new IOException("The HTTP request header has already been sent and cannot add a new request header");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

//        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        addHeader("Access-Control-Allow-Origin", "*");
        addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    public void end() throws IOException {
        if (isClosed()) throw new IOException("Response stream is closed!");
        closed = true;
        //响应长度，响应体为0个字节则设置长度为-1 否则浏览器会尝试重发
        long responseLength = ByteArray.size() == 0 ? -1 : ByteArray.size();
        //发送响应头
        try {
            if (!isEventSource) exchange.sendResponseHeaders(code, responseLength);
        } catch (IOException e) {
            throw new IllegalStateException("The response header has been sent");
        }
        //如果存在长度则响应内容
        if (responseLength>=0){
            getResponseBody().write(ByteArray.toByteArray());
            getResponseBody().flush();
        }
        ByteArray.close();
    }

    /**
     * 重定向
     */
    public void redirect(String path) throws IOException {
        exchange.getResponseHeaders().add("Location",path);
        exchange.sendResponseHeaders(302, 0);
    }

    public Headers getHeaders() {
        return exchange.getResponseHeaders();
    }

    public Headers addHeader(String key, String value) {
        if (isEventSource || isClosed())
            try {
                throw new IOException("The HTTP request header has already been sent and cannot add a new request header");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        getHeaders().add(key, value);
        return getHeaders();
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }


    public boolean isClosed() {
        return closed;
    }
}
