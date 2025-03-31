package com.easychat.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterceptor {

    boolean checkLogin() default true;//校验登录
    boolean checkAdmin() default false; //校验管理员

}
