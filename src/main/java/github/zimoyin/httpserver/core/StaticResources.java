package github.zimoyin.httpserver.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 静态资源加载器
 * 不支持运行时变更根路径
 */
public final class StaticResources {
    /**
     * 静态资源路径的位置，他可以是一个相对位置或者绝对位置。
     * 还可以是一个 resources 的路径，注意，如果是resources 路径需要在 路径前加 ’resources:‘  以此来标明他是来自于resources下的路径
     *
     * 默认路径为 resources 下的 static 文件夹
     */
    public static String staticResourcePath = "resources:static";
    /**
     * 主页的默认路径
     */
    public static final String indexPath = "/index.html";

    public static InputStream getStaticResourceAsStream(String path) throws IOException {
        //如果资源在 Resources 目录下
        if (staticResourcePath.startsWith("resources:")) {
            return getResourceAsStream(staticResourcePath.substring("resources:".length()) + path);
        } else {
            if (new File(staticResourcePath + path).exists()) return new FileInputStream(staticResourcePath + path);
        }
        return null;
    }

    private static InputStream getResourceAsStream(String path) throws IOException {
//        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        return StaticResources.class.getClassLoader().getResourceAsStream(path);
    }

    public static String defaultPath() {
        return "resources:static";
    }
}
