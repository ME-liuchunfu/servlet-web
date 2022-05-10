package xin.spring.servlet.web;

import xin.spring.servlet.web.annotation.AdviseException;
import xin.spring.servlet.web.annotation.AdviseHandler;
import xin.spring.servlet.web.core.ExceptionAdviseHandler;

import java.util.HashMap;
import java.util.Map;

@AdviseHandler(restResult = true)
public class GlobExceptionController {

    @AdviseException(value = Exception.class)
    public Map<String, Object> exception(ExceptionAdviseHandler.HandlerSource source) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("source", source.getSource());
        map.put("request", source.getRequest().getRequestURL());
        map.put("response", source.getResponse().getStatus());
        map.put("exception", source.getExc());
        return map;
    }

    @AdviseException(value = NoSuchMethodException.class)
    public Map<String, Object> noSuchMethodException(ExceptionAdviseHandler.HandlerSource source) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("source", source.getSource());
        map.put("request", source.getRequest().getRequestURL());
        map.put("response", source.getResponse().getStatus());
        map.put("exception", source.getExc());
        return map;
    }

}
