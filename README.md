# JDK 内置的HttpServer

1. 关于JDK 中的 HttpServer
   * HttpServer 是在 JDK 1.6 版本中被引入的，它提供了一个基于 HTTP 协议的轻量级 HTTP 服务器实现，可以用于创建和部署 Web 应用程序。
   * 在 JDK 9 中，HttpServer 支持 HTTP/2 协议，提供了对 WebSockets 和 SSL/TLS 加密的支持，并提供了更好的异常处理机制。
   * 在 JDK 11 中，HttpServer 进一步增强了对 HTTP/2 的支持，并增加了对 WebSocket 的异步处理支持。
   * 需要注意的是，虽然 HttpServer 提供了一种轻量级的 HTTP 服务器实现，但它通常不适合用于生产环境中的大型 Web 应用程序，因为它缺少一些高级特性，例如支持 Servlet 规范、JSP 和 EJB 等技术。对于生产环境中的大型 Web 应用程序，通常需要使用专业的 Web 服务器，例如 Apache、Nginx 或 Tomcat 等。
2. 关于本项目
   * 本项目是针对 JDK 中的 HttpServer的一个封装
   * 项目是另外的两个项目的一部分，并在后来进行了单独存储
   * 项目仅对 HttpServer 进行了一个初步的封装，这些封装可以让你更方便的使用JDK的API
3. 封装内容
   * 请求与响应封装
   * 拦截器封装
   * 静态资源访问封装
4. 如何使用他们

## 如何使用
1. 创建服务器实例
```java
//创建一个服务器实例。参数可置空。参数为该服务器的根路径
SimpleHttpServer server = new SimpleHttpServer("/");
//设置服务器的端口
server.setPort(8080);
//设置服务器的路由
server.addRoute(new ParameterReceiver());
//设置服务器的拦截器
server.addFilter(new FilterTest());
//启动服务器
server.start();
```
服务器实例其他可能重要的API
```java
//设置队列数
server.setRequestQueue(50);
//设置线程池
server.setExecutor(...);
//停止服务器
server.stop(0);
```

2. 路由编写（请求与响应）
通过继承AbsHttpHandler 来实现
```java
public class Router extends AbsHttpHandler {
    public Router() {
        //路由地址
        super("/test");
    }

    /**
     * 如果请求不是Get 或者 Post 的话他会执行此方法
     */
    @Override
    protected void doRequest(Request request, Response response);
    /**
     * 请求是Get方法
     */
    @Override
    protected void doGet(Request request, Response response);
    /**
     * 请求是Post方法
     */
    @Override
    protected void doPost(Request request, Response response);
}
```
注意你需要在服务器中设置路由才能使用，并且服务器启动后你是不能设置路由的。
```java
//设置服务器的路由
server.addRoute(new ParameterReceiver());
```

3. 拦截器
```java
public class FilterTest extends AbsFilter {
    public FilterTest() {
        //拦截地址。注意可以使用 通配符 '*'
        super("/test");
    }

   /**
    * 拦截器逻辑
    * 注意：如果你不确定是否返回 false 就请不要使用 response 对浏览器进行响应，否则会发生无法预测的事情
    * @return true 运行访问。false 禁止访问
    */
    @Override
    public boolean filter(Request request, Response response) {
        System.out.println("Filter test");
        return true;
    }
}
```
同样拦截器需要进行注册
```java
server.addFilter(new FilterTest());
```

3. 静态资源访问  
静态资源访问路径可以通过 `StaticResources.staticResourcePath` 进行设置
```java
/**
* 静态资源路径的位置，他可以是一个相对位置或者绝对位置。
* 还可以是一个 resources 的路径，注意，如果是resources 路径需要在 路径前加 ’resources:‘  以此来标明他是来自于resources下的路径
*
* 默认路径为 resources 下的 static 文件夹
*/
StaticResources.staticResourcePath = "resources:static";
```

4. 关于日志输出  
日志使用了来自JDK的日志进行输出，如果你禁用了可能会导致部分日志无法被正常输出，这部分日志可能包括异常信息
```java
//禁用日志输出
SimpleHttpServer.disableLogging()
```

5. 404  
如果想要自定义404，你需要创建一个路由，并且路由的 路径为 '/*',并且重写doRequest方法
```java
public class NotFoundRouter extends AbsHttpHandler {
    public NotFoundRouter() {
        super("/*");
    }

   @Override
   protected void doRequest(Request request, Response response) throws IOException {
      response.write("没有这个资源QAQ");
   }
}
```

6. 500  
如果你的路由中发生了错误,服务器会发送错误信息到前端。  
如果你不想发生错误信息到前端，你就需要在你的路由中重写 doError 方法。注意是每一个路由  
异常处理逻辑位于 `github.zimoyin.httpserver.core.AbsHttpHandler.doError` 他是非常原始的，请仔细查阅关于JDK中HttpExchange的API后进行操作  
  
通用处理异常方法样例
```java
@Override
protected void doError(Exception e, HttpExchange exchange) throws IOException {
  this.redirectTo(exchange,"/");//重定向，可以重定向到某个静态资源代表出现了错误
  super.doError(e, exchange);
}
```