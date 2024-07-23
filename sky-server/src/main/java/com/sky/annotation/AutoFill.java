package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解。用于标识某个方法需要进行功能字段自动填充处理
 */
//指定作用域为方法上面
@Target(ElementType.METHOD)
//默认必须加，让他在运行时也存在，不然的话他只会在编译时存在，在编译后被销毁
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    //指定数据库的操作类型：Update，INSERT，他是自己定义的一个枚举类型，在sky-commeon.enumeration中

    OperationType value();

}
