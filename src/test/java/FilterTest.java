import github.zimoyin.httpserver.core.AbsFilter;
import github.zimoyin.httpserver.core.Request;
import github.zimoyin.httpserver.core.Response;

import java.io.IOException;

public class FilterTest extends AbsFilter {
    public FilterTest() {
        super("/test");
    }

    @Override
    public boolean filter(Request request, Response response) {
        System.out.println("Filter test");
        return true;
    }
}
