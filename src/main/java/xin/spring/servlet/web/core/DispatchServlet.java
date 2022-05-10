package xin.spring.servlet.web.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import xin.spring.servlet.web.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@WebServlet(value = {"/server", "/server/*"})
public class DispatchServlet extends HttpServlet {

    private static final String RESFULT_START = "{";
    private static final String RESFULT_END = "}";

    public DispatchServlet() {
        System.out.println("init demo servlet");
        initMapping();
    }

    public ConcurrentHashMap<String, HandlerMethod> mappingMap = new ConcurrentHashMap<>();

    public void initMapping() {
        ScanClass scanClass = ScanClass.getScanClass(this.getClass().getClassLoader(), false);
        String prefName = DispatchServlet.class.getAnnotation(WebServlet.class).value()[0];
        scanClass.getClassList().stream().filter(v->v.isAnnotationPresent(RequestMapping.class))
                .forEach(v->{
                    RequestMapping mapping = v.getAnnotation(RequestMapping.class);
                    String value = mapping.value();
                    Object instant = getInstance(v);
                    Method[] declaredMethods = instant.getClass().getDeclaredMethods();
                    ConcurrentHashMap<String, HandlerMethod> map = new ConcurrentHashMap<>();
                    Arrays.stream(declaredMethods).filter(m-> m.isAnnotationPresent(GetMapping.class) || m.isAnnotationPresent(PostMapping.class))
                            .forEach(m-> {
                                GetMapping getMapping = m.getAnnotation(GetMapping.class);
                                PostMapping postMapping = m.getAnnotation(PostMapping.class);
                                if (Objects.nonNull(getMapping)) {
                                    String valKey = getMapping.value();
                                    String requestURI = (prefName + "/" + value + "/" + valKey).replaceAll("/+", "/");
                                    map.put(requestURI, new HandlerMethod(instant, m));
                                }
                                else if (Objects.nonNull(postMapping)) {
                                    String valKey = postMapping.value();
                                    String requestURI = (prefName + "/" + value + "/" + valKey).replaceAll("/+", "/");
                                    map.put(requestURI, new HandlerMethod(instant, m));
                                }
                            });
                    mappingMap.putAll(map);
                });
    }

    private Object getInstance(Class<?> v) {
        try {
            Object instant = v.getConstructor().newInstance();
            return instant;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("/demo service");
        String method = req.getMethod();
        System.out.println("method:" + method);

        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        System.out.println(requestURI);
        String subRequestURI = requestURI.replaceFirst(contextPath, "");
        HandlerMethod handlerMethod = mappingMap.get(subRequestURI);
        if (Objects.nonNull(handlerMethod)) {
            boolean support = handlerMethod.supportRequest(method);
            Object obj = null;
            boolean responseBody = false;
            try {
                if (!support) {
                    throw new NoSuchMethodException("the request method not support " + method);
                }
                obj = handlerMethod.invoke(req, resp);
                responseBody = handlerMethod.getMethod().isAnnotationPresent(ResponseBody.class);
            } catch (Exception exception) {
                obj = ExceptionAdviseHandler.getExceptionAdviseHandler().handlerException(handlerMethod, handlerMethod.method, handlerMethod.method.toString(), req, resp, exception);
                responseBody = true;
            }
            if (Objects.isNull(obj)) {
                return;
            }
            PrintWriter writer = resp.getWriter();
            if (responseBody) {
                writer.write(JSON.toJSONString(obj));
            } else {
                writer.write("the method not use annotation ResponseBody");
            }
            writer.flush();
        }
        else {
            PrintWriter writer = resp.getWriter();
            writer.write("error request not fount");
            writer.flush();
        }
    }

}

class HandlerMethod {

    Object obj;

    Method method;

    Set<RequestMapping.RequestType> requestSupportSet;

    public HandlerMethod(Object obj, Method method) {
        this.obj = obj;
        this.method = method;
        this.requestSupportSet = Arrays.stream(method.getAnnotations()).map(v-> {
            if (v instanceof GetMapping) {
                return RequestMapping.RequestType.GET;
            }
            else if (v instanceof PostMapping) {
                return RequestMapping.RequestType.POST;
            }
            else if (v instanceof DeleteMapping) {
                return RequestMapping.RequestType.DELETE;
            }
            else if (v instanceof OptionsMapping) {
                return RequestMapping.RequestType.OPTIONS;
            }
            else if (v instanceof HeadMapping) {
                return RequestMapping.RequestType.HEAD;
            }
            else if (v instanceof PutMapping) {
                return RequestMapping.RequestType.PUT;
            }
            else if (v instanceof TraceMapping) {
                return RequestMapping.RequestType.TRACE;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Set<RequestMapping.RequestType> getRequestSupportSet() {
        return requestSupportSet;
    }

    public void setRequestSupportSet(Set<RequestMapping.RequestType> requestSupportSet) {
        this.requestSupportSet = requestSupportSet;
    }

    public Object invoke(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        if (method.getParameterCount() > 0) {
            Object[] args = new Object[method.getParameterCount()];
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].equals(HttpServletRequest.class)) {
                    args[i] = req;
                }
                else if (parameterTypes[i].equals(HttpServletResponse.class)) {
                    args[i] = resp;
                }
                else {
                    Set<String> keySet = req.getParameterMap().keySet();
                    JSONObject object = new JSONObject();
                    for (String key : keySet) {
                        String val = req.getParameter(key);
                        object.put(key, val);
                    }
                    args[i] = object.toJavaObject(parameterTypes[i]);
                }
            }
            return method.invoke(obj, args);
        } else {
            return method.invoke(obj);
        }
    }

    public boolean supportRequest(String reqMethod) {
        RequestMapping.RequestType requestType = RequestMapping.RequestType.valueOf(reqMethod);
        if (Objects.isNull(requestType)) {
            return false;
        }
        return requestSupportSet.contains(requestType);
    }

}
