package org.example.save;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HttpServletRequest {
    private Map<String, Object> save;

    public HttpServletRequest() {
        save = new HashMap<>();
        System.out.println("HttpServletRequest创建了");
    }

    public void setAttribute(String key, Object value){
        save.put(key, value);
    }
    public Object getAttribute(String key){
        return save.get(key);
    }
}
