package com.sky.aspect;


import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j

public class AutoFillAspect {
    //拦截 com.sky.mapper 包下所有 Mapper 接口里的所有方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {}

    //在通知中为公共字段赋值
    //对所有符合 autoFillPointCut 规则的方法，执行这个 @Before
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始为公共字段填充...");

        //获得方法信息对象
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //获得拦截方法参数
            AutoFill autofill =signature.getMethod().getAnnotation(AutoFill.class);
            OperationType operationType = autofill.value();

            Object[] args = joinPoint.getArgs();
            if(args==null || args.length==0){
                log.info("自动填充获得参数为空！");
                return ;
            }
            Object entity = args[0];
            //赋值
        LocalDateTime now = LocalDateTime.now();
        long currentid = BaseContext.getCurrentId();
        if(operationType==OperationType.INSERT){

            try{
                Method setCreateTime =  entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentid);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentid);
            }catch(Exception e)
            {
                    e.printStackTrace();
            }
        }else if(operationType==OperationType.UPDATE){
            try{
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentid);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
