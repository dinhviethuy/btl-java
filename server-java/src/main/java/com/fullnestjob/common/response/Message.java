package com.fullnestjob.common.response;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Message {
    String value() default "";
}


