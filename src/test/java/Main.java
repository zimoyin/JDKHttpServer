import github.zimoyin.httpserver.core.SimpleHttpServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SimpleHttpServer server = new SimpleHttpServer("/");
        server.setPort(8080);
        server.addRoute(new ParameterReceiver());
        server.addRoute(new NotFoundRouter());
        server.addFilter(new FilterTest());
        server.start();
    }
}