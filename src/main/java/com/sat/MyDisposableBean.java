package com.sat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/**
 * 结束的时候执行
 * @author dmw
 *
 * 2019年4月15日
 */
@Component
@Slf4j
public class MyDisposableBean implements DisposableBean{

    @Override
    @PreDestroy
    public void destroy() throws Exception {
        System.out.println("结束");

    }

}