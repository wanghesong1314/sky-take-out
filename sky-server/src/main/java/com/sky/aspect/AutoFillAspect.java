package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import sun.util.resources.LocaleData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

//使用Aspect注解来标识一个切面类，COmponet将他扫描搭到spring中，slf4j为一个日志的通知类
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     * 制定service中所有参数类型的所有方法并且是添加了AutoFill注解的方法
     */
    @Pointcut("execution(* com.sky.service.impl.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     * 前置通知,添加要通知的切入点，可以是一个数组
     */
    @Before("autoFillPointCut()")
    public void beforeAutoFill(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println(joinPoint);
        log.info("开始进行公共字段填充...");
//        获取当前别拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

//        获取到当前被拦截方法的参数--实体对象,获得所有参数
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
//        约定号将要封装的实体作为封装的第一个参数
        Object entity = args[0];

//        准备赋值的值

        LocalDateTime localDateTime = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

//        根据不同的类型，为对应得属性通过反射来赋值
//        通过getFields（）和getMethods（）方法获得权限为public成员变量和成员方法时，还包括从父类继承得到的成员变量和成员方法；
//        而通过getDeclaredFields（）和getDeclaredMethods（）方法只是获得在本类中定义的所有成员变量和成员方法。
//        使用常量防止出错
        if (operationType == OperationType.INSERT) {
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateTIme = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

//            通过反射为对象属性赋值，
            setCreateTime.invoke(entity, localDateTime);
            setCreateUser.invoke(entity, currentId);
            setUpdateTIme.invoke(entity, localDateTime);
            setUpdateUser.invoke(entity, currentId);

        } else if (operationType == OperationType.UPDATE) {
            Method setUpdateTIme = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateTIme.invoke(entity, localDateTime);
            setUpdateUser.invoke(entity, currentId);
        }


    }

}
