package github.zimoyin;

import github.zimoyin.httpserver.core.FilterManager;
import github.zimoyin.httpserver.core.SimpleHttpServer;
import github.zimoyin.test.FilterTest;
import github.zimoyin.test.NotFoundRouter;
import github.zimoyin.test.ParameterReceiver;

import java.io.IOException;
import java.util.UUID;

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