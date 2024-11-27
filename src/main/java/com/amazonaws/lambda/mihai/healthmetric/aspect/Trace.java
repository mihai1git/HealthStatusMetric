package com.amazonaws.lambda.mihai.healthmetric.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * used in AspectJ architecture to trace methods execution<br>
 * used as annontation for methods, in classes where only marked methods are traced
 * @author mike
 *
 */
@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.TYPE)
@Target(ElementType.METHOD)
public @interface Trace {
}
