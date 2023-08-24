package com.sat.utils;

import lombok.Data;

@Data
public class R {
    private Boolean flag;
    private Object data;
    private String  log;

    public R(Boolean flag, Object data, String log) {
        this.flag = flag;
        this.data = data;
        this.log = log;
    }

    public R() {
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
