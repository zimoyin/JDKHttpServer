package github.zimoyin.httpserver.core;

import com.sun.net.httpserver.HttpServer;
import github.zimoyin.httpserver.SimpleConsoleFormatter;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Http Server
 */
public final class SimpleHttpServer {
    public static boolean isLoggingEnabled = true;
    private int port = 8080;
    private Executor executor = null;
    private final HashSet<AbsHttpHandler> routes = new HashSet<>();
    private final String RootPath;
    private boolean isRunning = false;

    /**
     * 队列数量
     */
    private int requestQueue = 50;
    private final Logger logger = SimpleConsoleFormatter.installFormatter(Logger.getLogger(SimpleHttpServer.class.getTypeName()));
    private HttpServer server;
    /**
     * 服务器ID
     */
    private final UUID ID;

    public SimpleHttpServer() {
        ID = UUID.randomUUID();
        RootPath = "/";
        this.executor = Executors.newFixedThreadPool(getRequestQueue());
    }

    /**
     * 设置 WEB 的根路径，默认为 /
     */
    public SimpleHttpServer(String rootPath) {
        ID = UUID.randomUUID();
        RootPath = rootPath;
        this.executor = Executors.newFixedThreadPool(getRequestQueue());
    }


    /**
     * 启动服务器
     */
    public HttpServer start() throws IOException {
        if (isRunning) throw new IllegalStateException("HttpServer is already running");
        isRunning = true;

        HttpServer.create();
        try {
            // 绑定地址，端口，请求队列
            server = HttpServer.create(new InetSocketAddress(port), requestQueue);
            logger.info("HttpServer request queue size: " + requestQueue);
        } catch (BindException e) {
            logger.log(Level.WARNING, "Address already in use: " + port);
            throw e;
        }
        logger.info("Loading route...");
        //注册路由
        try {
            for (AbsHttpHandler route : routes) server.createContext(route.getRoute(), route.setID(ID));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Do not set the server ID for routes outside of the server instance, as this may prevent routes from being loaded properly", e);
        }
        //注册静态资源路由与根路由
        server.createContext(RootPath, new StaticResourcesRoute(routes, RootPath).setID(ID));
        //路由日志
        logger.info("Loaded routes finished successfully: " + (routes.size() + 1));
        if (routes.size() == 0) logger.warning("No routes found for this application (port: " + port + ")");
        // 配置HttpServer请求处理的线程池，没有配置则使用默认的线程池；
        if (executor != null) server.setExecutor(executor);
        logger.info("Loading executor : " + executor);
        server.start();
        //日志
        if (port > 0) logger.info("Server have been started. Listen to port: " + port);
        else logger.info("Server have been started. Listen to port: " + port + " -> " + server.getAddress().getPort());
        return getServer();
    }

    /**
     * 添加路由
     */
    public void addRoute(AbsHttpHandler route) {
        routes.add(route);
    }

    public void addRouteAll(AbsHttpHandler... routes) {
        this.routes.addAll(Arrays.asList(routes));
    }

    /**
     * 添加过滤器
     */
    public void addFilter(AbsFilter filter) {
        FilterManager.getInstance().add(ID, filter);
    }

    public void addFilterAll(AbsFilter... filters) {
        FilterManager.getInstance().addAll(ID, Arrays.asList(filters));
    }

    /**
     * 移除路由
     */
    public void removeRoute(AbsHttpHandler route) {
        routes.remove(route);
    }

    /**
     * 停止WEB服务器
     *
     * @param delay 在多少秒内停止
     */
    public void stop(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (this.server == null) throw new NullPointerException("this http server is null or is stopped");
        server.stop(0);
        logger.info("Server have been stopped");
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Executor getExecutor() {
        return executor;
    }

    /**
     * 设置处理的线程池
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public HashSet<AbsHttpHandler> getRoutes() {
        return routes;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getRequestQueue() {
        return requestQueue;
    }

    /**
     * 设置访问队列大小
     */
    public void setRequestQueue(int requestQueue) {
        this.requestQueue = requestQueue;
    }

    private HttpServer getServer() {
        return server;
    }

    public String getRootPath() {
        return RootPath;
    }

    /**
     * 禁用日志输出
     */
    public static synchronized void disableLogging() {
        Logger.getLogger("github.zimoyin.httpserver").setLevel(Level.OFF);
        Logger.getLogger("github.zimoyin.httpserver.core").setLevel(Level.OFF);
        isLoggingEnabled = false;
    }
}
