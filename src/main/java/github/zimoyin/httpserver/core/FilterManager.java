package github.zimoyin.httpserver.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class FilterManager extends HashMap<UUID, ArrayList<AbsFilter>> {
    private volatile static FilterManager INSTANCE;

    private FilterManager() {
    }

    public static FilterManager getInstance() {
        if (INSTANCE == null) synchronized (FilterManager.class) {
            if (INSTANCE == null) INSTANCE = new FilterManager();
        }
        return INSTANCE;
    }


    public AbsFilter[] findFilters(UUID ID, String route) {
        ArrayList<AbsFilter> filters = this.get(ID);
        if (filters == null) return new AbsFilter[0];
        return filters.stream().filter(filter -> isFilter(filter, route)).toArray(AbsFilter[]::new);
    }


    private boolean isFilter(AbsFilter filter, String route) {
        boolean ignoreCase = filter.getRoute().equalsIgnoreCase(route);
        return ignoreCase || matchRoute(route, filter.getRoute());
    }


    public void add(UUID ID, AbsFilter filter) {
        FilterManager.getInstance().computeIfAbsent(ID, k -> new ArrayList<>()).add(filter);
    }

    public static boolean matchRoute(String route1, String route2) {
        String[] parts1 = route1.split("/");
        String[] parts2 = route2.split("/");

        if (parts1.length < parts2.length) return false;
        if (parts1.length > parts2.length && !route2.contains("*")) return false;

        boolean b = false;

        //长度一致
        //从第二路由开始判断
        for (int i = 0; i < parts2.length; i++) {
            String item1 = parts1[i];
            String item2 = parts2[i];
            //判断路径
            if ("*".equals(item2)) {
                b = true;
//                break;
            } else if (item1.equals(item2)) {
                b = true;
            } else {
                b = false;
                break;
            }
        }

        return b;
    }

    public void addAll(UUID id, List<AbsFilter> list) {
        FilterManager.getInstance().computeIfAbsent(id, k -> new ArrayList<>()).addAll(list);
    }
}
