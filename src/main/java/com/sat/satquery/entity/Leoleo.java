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
    public class Leoleo implements Serializable {

    private static final long serialVersionUID = 1L;

            @TableId("IDsat")
    private Integer IDsat;

        @TableField("SSID")
    private String ssid;

        @TableField("Tida")
    private String Tida;

        @TableField("Tidb")
    private String Tidb;

        @TableField("ST")
    private Integer st;

        @TableField("Token")
    private String Token;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getIDsat() {
        return IDsat;
    }

    public void setIDsat(Integer IDsat) {
        this.IDsat = IDsat;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getTida() {
        return Tida;
    }

    public void setTida(String tida) {
        Tida = tida;
    }

    public String getTidb() {
        return Tidb;
    }

    public void setTidb(String tidb) {
        Tidb = tidb;
    }

    public Integer getSt() {
        return st;
    }

    public void setSt(Integer st) {
        this.st = st;
    }

    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }
}
