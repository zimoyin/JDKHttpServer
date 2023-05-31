package github.zimoyin.httpserver.core;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import github.zimoyin.httpserver.SimpleConsoleFormatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 基层 Http 处理器
 * 如果想要对http 做出响应需要继承此类
 * 并且你还需要在服务器启动前注册进服务器的路由里.
 * eg:
 * SimpleHttpServer server = new SimpleHttpServer("/");
 * server.addRoute(new MyHttpHandler());
 */
public abstract class AbsHttpHandler implements HttpHandler {
    protected final Logger logger = SimpleConsoleFormatter.installFormatter(Logger.getLogger(this.getClass().getTypeName()));
    private final String route;
    private final boolean debugLog = false;

    /**
     * 服务器ID
     */
    private UUID ID;

    public AbsHttpHandler(String route) {
        this.route = route.trim().toLowerCase();
        if (route.length() == 0) logger.warning(route + " : 路由是个空路径 ");
        if (!"/".equals(route.substring(0, 1))) logger.warning(route + " : 路由没有路径符号 '/' ");
    }


    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        try {
            if (debugLog) logger.info("Http handler route: " + route);
            if (getID() == null) throw new NullPointerException("HTTP Server ID is null");
            //根据根据请求方法来分发请求
            doMethod(exchange.getRequestMethod(), exchange);
        } catch (Exception e) {
            doError(e, exchange);
        }
    }

    private void doMethod(String methodName, HttpExchange exchange) throws IOException {
        Response response = new Response(exchange);
        Request request = new Request(exchange);

        boolean error = false;

        //过滤器
        boolean accept = true;
        for (AbsFilter filter : FilterManager.getInstance().findFilters(getID(), request.getPath())) {
            accept = filter.filter(request, response) && accept;
        }

        if (accept) switch (methodName.toUpperCase()) {
            case "GET" -> {
                if (debugLog) logger.info("Get Path: " + exchange.getRequestURI().getSchemeSpecificPart());
                try {
                    doGet(request, response);
                } catch (Exception e) {
                    error = true;
                    doError(e, exchange);
                }
            }
            case "POST" -> {
                if (debugLog) logger.info("Post Path: " + exchange.getRequestURI().getSchemeSpecificPart());
                try {
                    doPost(request, response);
                } catch (Exception e) {
                    error = true;
                    doError(e, exchange);
                }
            }
            case "OPTIONS" -> {
                if (debugLog) logger.info("OPTIONS Path: " + exchange.getRequestURI().getSchemeSpecificPart());
                try {
                    doOptions(request, response);
                } catch (Exception e) {
                    error = true;
                    doError(e, exchange);
                }
            }
            default -> doRequest(request, response);
        }

        //如果响应是因为异常被关闭的则截拦状态异常不进行返回，如果不是则抛出
        try {
            close(response, exchange);
        } catch (IllegalStateException e) {
            if (debugLog) if (error) logger.log(Level.INFO, "Please ignore this exception", e);
            if (!error) throw e;
        }

    }

    protected final void close(Response response, HttpExchange exchange) throws IOException {
        if (!response.isClosed()) response.end();
        exchange.close();
    }

    /**
     * 将 Exception 转化为 String
     */
    protected final String getExceptionToString(Throwable e) {
        if (e == null) {
            return "";
        }
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    /**
     * 处理路由中的错误
     */
    protected void doError(Exception e, HttpExchange exchange) throws IOException {
        //发送响应标头。必须在之前调用 下一步。
        int length = ("Server Error: \n\n" + getExceptionToString(e)).length();
        try {
            exchange.sendResponseHeaders(500, length);
            exchange.getResponseBody().write(("Server Error: \n\n" + getExceptionToString(e)).getBytes());
            logger.log(Level.WARNING, "Server Error, with the error path being: " + exchange.getRequestURI().getSchemeSpecificPart(), e);
        } catch (IOException e2) {
            e2.initCause(e);
            logger.log(Level.WARNING, "An incorrect write exception occurred, with the error path being: " + exchange.getRequestURI().getSchemeSpecificPart(), e2);
        }
    }

    /**
     * 默认处理器
     * 如果请求不是Get 或者 Post 等任意已写的方法处理器 的话他会执行此方法
     */
    protected void doRequest(Request request, Response response) throws IOException {
//        logger.info("/" + request.getMethod() + " Path: " + request.getPath());
        logger.warning("/" + request.getMethod() + " Path: " + request.getPath() + " -> 无法解析的 Method :" + request.getMethod());
        response.setCode(-405);
        response.write("This Request Method not support.");
    }

    protected void doGet(Request request, Response response) throws IOException {
        response.setCode(-405);
        response.write("This Request Method not support.");
    }

    protected void doPost(Request request, Response response) throws IOException {
        response.setCode(-405);
        response.write("This Request Method not support.");
    }

    protected void doOptions(Request request, Response response) throws IOException {
        response.setCrossDomain();
        response.setCode(204);
    }

    public String getRoute() {
        return route;
    }

    /**
     * 重定向
     */
    public void redirectTo(HttpExchange exchange, String path) throws IOException {
        exchange.getResponseHeaders().add("Location", path);
        exchange.sendResponseHeaders(302, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbsHttpHandler that)) return false;

        return route.equals(that.route);
    }

    @Override
    public int hashCode() {
        return route.hashCode();
    }

    /**
     * 服务器ID
     */
    public final UUID getID() {
        return ID;
    }

    /**
     * 服务器ID
     */
    public final AbsHttpHandler setID(UUID ID) {
        if (this.ID != null)
            throw new IllegalArgumentException("The HTTP Server ID is final and cannot be changed. And ID can only be set by the server during server startup");
        this.ID = ID;
        return this;
    }
}
