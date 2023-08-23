package com.sat.satquery.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
* <p>
    * 
    * </p>
*
* @author Archie
* @since 2023-08-15
*/
    @Data
        @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    public class Pretcc implements Serializable {

    private static final long serialVersionUID = 1L;

            @TableId("ID")
    private Integer id;

        @TableField("DKauth")
    private String DKauth;

        @TableField("DKenc")
    private String DKenc;

        @TableField("Wkenc")
    private String WKenc;

        @TableField("K")
    private String k;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDKauth() {
        return DKauth;
    }

    public void setDKauth(String DKauth) {
        this.DKauth = DKauth;
    }

    public String getDKenc() {
        return DKenc;
    }

    public void setDKenc(String DKenc) {
        this.DKenc = DKenc;
    }

    public String getWKenc() {
        return WKenc;
    }

    public void setWKenc(String WKenc) {
        this.WKenc = WKenc;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }
}
