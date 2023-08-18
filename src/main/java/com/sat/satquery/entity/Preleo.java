package com.sat.satquery.entity;

    import com.baomidou.mybatisplus.annotation.TableId;
    import com.baomidou.mybatisplus.annotation.TableField;
    import java.io.Serializable;
    import lombok.Data;
    import lombok.EqualsAndHashCode;
    import lombok.experimental.Accessors;

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
    public class Preleo implements Serializable {

    private static final long serialVersionUID = 1L;

            @TableId("IDsat")
    private Integer IDsat;

        @TableField("SSID")
    private Integer ssid;

        @TableField("Dkenc")
    private String Dkenc;

        @TableField("Dkauth")
    private String Dkauth;

        @TableField("WKenc")
    private String WKenc;

        @TableField("K")
    private String k;

        @TableField("MainKey")
    private String MainKey;


        @TableField("CK")
    private String ck;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getIDsat() {
        return IDsat;
    }

    public void setIDsat(Integer IDsat) {
        this.IDsat = IDsat;
    }

    public Integer getSsid() {
        return ssid;
    }

    public void setSsid(Integer ssid) {
        this.ssid = ssid;
    }

    public String getDkenc() {
        return Dkenc;
    }

    public void setDkenc(String dkenc) {
        Dkenc = dkenc;
    }

    public String getDkauth() {
        return Dkauth;
    }

    public void setDkauth(String dkauth) {
        Dkauth = dkauth;
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

    public String getMainKey() {
        return MainKey;
    }

    public void setMainKey(String mainKey) {
        MainKey = mainKey;
    }

    public String getCk() {
        return ck;
    }

    public void setCk(String ck) {
        this.ck = ck;
    }
}
