package com.sat.utils;
import com.sat.MyDisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShutDownConfig{

@Bean
public MyDisposableBean getTerminateBean(){
        return new MyDisposableBean();
        }

}