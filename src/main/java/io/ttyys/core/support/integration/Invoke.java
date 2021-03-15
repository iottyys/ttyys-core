package io.ttyys.core.support.integration;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Invoke {

    /**
     * 服务接口名称
     *
     * @return
     */
    String serviceInterface();

    /**
     * 服务方法名称
     *
     * @return
     */
    String serviceMethod();

    /**
     * 转换文件地址
     *
     * @return
     */
    String convertFile();
}
