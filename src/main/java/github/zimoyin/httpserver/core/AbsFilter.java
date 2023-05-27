package github.zimoyin.httpserver.core;

import github.zimoyin.httpserver.SimpleConsoleFormatter;

import java.util.logging.Logger;

public abstract class AbsFilter {
    private final String route;
    protected final Logger logger = SimpleConsoleFormatter.installFormatter(Logger.getLogger(this.getClass().getTypeName()));

    public AbsFilter(String route) {
        this.route = route.trim().toLowerCase();
        if (route.length() == 0)logger.warning(route + " : 路由是个空路径 ");
        if (!"/".equals(route.substring(0,1))) logger.warning(route + " : 路由没有路径符号 '/' ");
    }

    public abstract boolean filter(Request request, Response response);

    public final String getRoute() {
        return route;
    }
}
