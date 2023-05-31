import github.zimoyin.httpserver.core.AbsHttpHandler;
import github.zimoyin.httpserver.core.Request;
import github.zimoyin.httpserver.core.Response;

import java.io.IOException;

public class NotFoundRouter extends AbsHttpHandler {
    public NotFoundRouter() {
        super("/*");
    }

    @Override
    protected void doRequest(Request request, Response response) throws IOException {
        response.write("没有这个资源QAQ");
    }
}
