package org.noear.solon.extend.validation.annotation;


import java.lang.annotation.*;

@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XNotNull {
    /**
     * param names
     * */
    String[] value();
}
