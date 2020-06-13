package webserver.session;

import java.util.HashMap;
import java.util.Map;

public class SessionStore {
    private static Map<String, HttpSession> sessionStore = new HashMap<>();

    public static HttpSession get(String id) {
        return sessionStore.get(id);
    }

    public static void add(String id, HttpSession httpSession) {
        sessionStore.put(id, httpSession);
    }

}
