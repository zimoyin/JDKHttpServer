import com.sun.net.httpserver.HttpExchange;
import github.zimoyin.httpserver.core.AbsHttpHandler;
import github.zimoyin.httpserver.core.Request;
import github.zimoyin.httpserver.core.Response;

import java.io.IOException;
import java.util.UUID;
import java.util.function.BiConsumer;

public class ParameterReceiver extends AbsHttpHandler {
    public ParameterReceiver() {
        super("/test");
    }


    @Override
    protected void doGet(Request request, Response response) throws IOException {
        this.setID(UUID.randomUUID());
        response.setEventSource();

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            response.send(String.valueOf(i));
//            System.out.println(i);
        }
        response.send(": end");
    }

    @Override
    protected void doPost(Request request, Response response) throws IOException {
//        request.getHeaders().forEach((s, strings) -> System.out.println(s+": "+strings));

        System.out.println(new String(request.getBody()));
        response.send(new String(request.getBody()));
    }

    @Override
    protected void doError(Exception e, HttpExchange exchange) throws IOException {
        this.redirectTo(exchange,"/");//重定向
        super.doError(e, exchange);
    }
}
