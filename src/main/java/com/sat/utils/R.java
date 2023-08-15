package com.sat.utils;

import lombok.Data;

@Data
public class R {
    private Boolean flag;
    private Object data;
    private Integer ans;
    private  String msg;
    public R(Boolean flag){
        this.flag = flag;
    }
    public R(){}
    public R(Boolean flag, Object data,Integer ans){
        this.flag = flag;
        this.data = data;
        this.ans = ans;
    }
    public R(Boolean flag, Object data){
        this.flag = flag;
        this.data = data;
    }
    public R(String msg){
        this.flag = false;
        this.msg = msg;
    }
}
