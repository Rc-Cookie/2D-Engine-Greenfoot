package com.github.rccookie.engine2d.impl.greenfoot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the given type causes TeaVM to fail compiling. At the same time,
 * it is NEVER used in the JavaScript build. Therefore, it <b>MUST NOT</b> be accessed in
 * a normal way, but only using "indirect" reflection, meaning the compiler cannot
 * predict what the reflection call will do.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface CompileSensitive {
}
