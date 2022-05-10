package xin.spring.servlet.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequestMapping {

    String value();

    RequestType method() default RequestType.GET;

    enum RequestType{
        DELETE, HEAD, GET, OPTIONS, POST, PUT, TRACE,
        ;
    }
}
