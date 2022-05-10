package xin.spring.servlet.web.core;

import xin.spring.servlet.web.annotation.AdviseException;
import xin.spring.servlet.web.annotation.AdviseHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ExceptionAdviseHandler {

    Logger logger = Logger.getLogger(ExceptionAdviseHandler.class.getName());

    private ConcurrentHashMap<Class<? extends Throwable>, List<Advise>> handlerMap = new ConcurrentHashMap<Class<? extends Throwable>, List<Advise>>();

    private static ExceptionAdviseHandler defaultExceptionAdviseHandler;

    public synchronized static ExceptionAdviseHandler getExceptionAdviseHandler() {
        if (Objects.isNull(defaultExceptionAdviseHandler)) {
            try {
                defaultExceptionAdviseHandler = new ExceptionAdviseHandler();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return defaultExceptionAdviseHandler;
    }

    public ExceptionAdviseHandler() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        ScanClass scanClass = ScanClass.getScanClass(this.getClass().getClassLoader(), false);
        List<Class<?>> list = scanClass.getClassList().stream().filter(v -> v.isAnnotationPresent(AdviseHandler.class)).collect(Collectors.toList());
        for (Class<?> clazz : list) {
            Object obj = scanClass.getObjInstance(clazz);
            //handler.add(obj);
            Advise advise = new Advise(obj, new HashSet<Object>());
            advise.parseException();
            Map<Class<? extends Throwable>, List<MethodParse>> exceptionMap = advise.getExceptionMap();
            Iterator<Map.Entry<Class<? extends Throwable>, List<MethodParse>>> iterator = exceptionMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Class<? extends Throwable>, List<MethodParse>> next = iterator.next();
                List<Advise> adviseList = handlerMap.get(next.getKey());
                if (Objects.isNull(adviseList)) {
                    adviseList = new ArrayList<Advise>();
                    handlerMap.put(next.getKey(), adviseList);
                }
                adviseList.add(advise);
            }
        }
    }

    public Object handlerException(Object source, Method method, Object msg, HttpServletRequest request, HttpServletResponse response, Exception... exc) {
        ArrayList<Object> list = new ArrayList<>();
        if (Objects.isNull(exc)) {
            return list;
        }
        for (Exception exception : exc) {
            List<Advise> adviseList = handlerMap.get(exception.getClass());
            if (Objects.isNull(adviseList) || adviseList.isEmpty()) {
                continue;
            }
            for (Advise advise : adviseList) {
                Object resObj = advise.invoke(new HandlerSource(source, method, msg, request, response, exception));
                list.add(resObj);
            }
        }
        return list.size() == 1 ? list.get(0) : list;
    }

    public static StringBuilder convertToStringBuilder(Exception exception) {
        StringBuilder builder = new StringBuilder();
        if (Objects.nonNull(exception)) {
            StackTraceElement[] stackTrace = exception.getStackTrace();
            Arrays.stream(stackTrace).forEach(v->builder.append(v.toString()).append("\n\t"));
        }
        return builder;
    }

    class Advise {

        private Object object;
        private Set<Object> annotationSet;
        private Map<Class<? extends Throwable>, List<MethodParse>> exceptionMap;
        private boolean parse;
        public Advise(Object object, Set<Object> annotationSet) {
            this.object = object;
            this.annotationSet = annotationSet;
            this.exceptionMap = new HashMap<Class<? extends Throwable>, List<MethodParse>>();
        }

        public Map<Class<? extends Throwable>, List<MethodParse>> parseException() {
            if (exceptionMap.isEmpty() && !parse) {
                Class<?> superClass = object.getClass();
                boolean result = object.getClass().getAnnotation(AdviseHandler.class).restResult();
                while (superClass != Object.class) {
                    Method[] methods = superClass.getDeclaredMethods();
                    Arrays.stream(methods).filter(v -> v.isAnnotationPresent(AdviseException.class))
                            .filter(Objects::nonNull).map(v -> {
                                AdviseException adviseHandler = v.getAnnotation(AdviseException.class);
                                Class<? extends Throwable> value = adviseHandler.value();
                                Map<Class<? extends Throwable>, MethodParse> map = new HashMap<Class<? extends Throwable>, MethodParse>();
                                map.put(value, new MethodParse(v, result));
                                return map;
                            }).forEach(v->{
                        Class<? extends Throwable> key = v.keySet().iterator().next();
                        MethodParse methodParse = v.get(key);
                        List<MethodParse> adviseList = exceptionMap.get(key);
                        if (Objects.isNull(adviseList)) {
                            adviseList = new ArrayList<MethodParse>();
                            exceptionMap.put(key, adviseList);
                        }
                        adviseList.add(methodParse);
                    });
                    superClass = superClass.getSuperclass();
                }
                parse = true;
            }
            return exceptionMap;
        }

        public Object invoke(HandlerSource handlerSource) {
            List<MethodParse> methodParseList = exceptionMap.get(handlerSource.getExc().getClass());
            if (Objects.isNull(methodParseList)) {
                return null;
            }
            ArrayList<Object> list = new ArrayList<>();
            for (MethodParse methodParse : methodParseList) {
                try {
                    Object resObj = null;
                    if (methodParse.method.getParameterCount() > 0) {
                        resObj = methodParse.getMethod().invoke(object, handlerSource);
                    }
                    else {
                        resObj = methodParse.getMethod().invoke(object);
                    }
                    list.add(resObj);
                } catch (Exception e) {
                    logger.warning(convertToStringBuilder(e).toString());
                    return e;
                }
            }
            return list.size() == 1 ? list.get(0) : list;
        }

        public Object getObject() {
            return object;
        }

        public void setObject(Object object) {
            this.object = object;
        }

        public Set<Object> getAnnotationSet() {
            return annotationSet;
        }

        public void setAnnotationSet(Set<Object> annotationSet) {
            this.annotationSet = annotationSet;
        }

        public Map<Class<? extends Throwable>, List<MethodParse>> getExceptionMap() {
            return exceptionMap;
        }

        public void setExceptionMap(Map<Class<? extends Throwable>, List<MethodParse>> exceptionMap) {
            this.exceptionMap = exceptionMap;
        }

        public boolean isParse() {
            return parse;
        }

        public void setParse(boolean parse) {
            this.parse = parse;
        }

    }

    class MethodParse {
        private Method method;
        private boolean result;
        private Class<?>[] parameterTypes;

        public MethodParse(Method method, boolean result) {
            this.method = method;
            this.result = result;
            this.parameterTypes = method.getParameterTypes();
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }

        public Class<?>[] getParameterTypes() {
            return parameterTypes;
        }

        public void setParameterTypes(Class<?>[] parameterTypes) {
            this.parameterTypes = parameterTypes;
        }

        public Object[] parseArgs(Object... args) {
            if (Objects.isNull(parameterTypes) || parameterTypes.length == 0) {
                return null;
            }
            Object[] objects = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                inner: for (int j = 0; j < args.length; j++) {
                    if (parameterTypes[i].equals(args[j].getClass())) {
                        objects[i] = args;
                        break inner;
                    }
                }
            }
            return objects;
        }
    }

    public static class HandlerSource {
        Object source;
        Method method;
        Object msg;
        HttpServletRequest request;
        HttpServletResponse response;
        Exception exc;

        public HandlerSource(Object source, Method method, Object msg, HttpServletRequest request, HttpServletResponse response, Exception exc) {
            this.source = source;
            this.method = method;
            this.msg = msg;
            this.request = request;
            this.response = response;
            this.exc = exc;
        }

        public Object getSource() {
            return source;
        }

        public void setSource(Object source) {
            this.source = source;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Object getMsg() {
            return msg;
        }

        public void setMsg(Object msg) {
            this.msg = msg;
        }

        public HttpServletRequest getRequest() {
            return request;
        }

        public void setRequest(HttpServletRequest request) {
            this.request = request;
        }

        public HttpServletResponse getResponse() {
            return response;
        }

        public void setResponse(HttpServletResponse response) {
            this.response = response;
        }

        public Exception getExc() {
            return exc;
        }

        public void setExc(Exception exc) {
            this.exc = exc;
        }
    }
}
