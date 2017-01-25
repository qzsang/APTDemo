package com.qzsang.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by quezhongsang on 2017/1/25.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface ViewInject {
    int value();
}
