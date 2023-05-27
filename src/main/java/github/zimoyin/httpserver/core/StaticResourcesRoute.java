package github.zimoyin.httpserver.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.logging.Level;

/**
 * 全局静态资源路由
 */
public final class StaticResourcesRoute extends AbsHttpHandler {
    private final HashSet<AbsHttpHandler> Routes;

    public StaticResourcesRoute(HashSet<AbsHttpHandler> routes, String rootPath) {
        super(rootPath);
        this.Routes = routes;
        if (!rootPath.endsWith("/")) {
            throw new IllegalArgumentException("The RootPath must end with '/'");
        }
    }

    @Override
    protected void doGet(Request request, Response response) throws IOException {
        //浏览器访问的获取路由
        String path = request.getURI().getPath();
        //如果访问根路径，则重定向到 Index 页面
        if (Objects.equals(path, "/")) path = StaticResources.indexPath;
        //判断是不是一个已经注册的路由
        String finalPath = path;
        boolean isRoute = Routes.stream().anyMatch(absHttpHandler -> absHttpHandler.getRoute().equals(finalPath));
        if (isRoute) logger.warning("Error： 静态资源与注册路由路径一致");
//        logger.info("客户端请求静态资源: "+path);
        //是否存在静态资源,存在和获取，不存在则 404
        InputStream stream = StaticResources.getStaticResourceAsStream(path);
        if (stream == null) {
            notFound(request, response);
            return;
        }
        //返回这个静态资源
        ByteArrayOutputStream arrayOutputStream = null;
        try {
            arrayOutputStream = new ByteArrayOutputStream();
            byte[] bytes = readStaticResource(stream, arrayOutputStream);
            response.write(bytes);
            response.end();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Static resource read failed with path: " + finalPath, e);
        } finally {
            if (arrayOutputStream != null) arrayOutputStream.close();
            stream.close();
        }

        if (!response.isClosed()) response.write("Error: 未能加载的静态资源");
        if (!response.isClosed())
            logger.warning("Error: 未能加载的静态资源: " + StaticResources.staticResourcePath + path);
    }

    private void notFound(Request request, Response response) throws IOException {
        AbsHttpHandler router = Routes.stream().filter(absHttpHandler -> absHttpHandler.getRoute().equals("/*")).findFirst().orElse(null);

        if (router == null) {
            logger.warning("客户端请求了一个不存在的路径: " + request.getURI());
            response.setCode(404);
            response.write("Not Found");
        } else {
            router.doRequest(request, response);
        }

    }

    private byte[] readStaticResource(InputStream stream, ByteArrayOutputStream arrayOutputStream) throws IOException {
        int len;
        byte[] bytes = new byte[1024];
        while ((len = stream.read(bytes)) != -1) {
            arrayOutputStream.write(bytes, 0, len);
        }
        return arrayOutputStream.toByteArray();
    }

    @Override
    protected void doPost(Request request, Response response) throws IOException {
        notFound(request, response);
    }
}
