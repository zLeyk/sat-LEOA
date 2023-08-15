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
* @since 2023-08-14
*/
    @Data
        @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    public class LeoLeoAuthentication implements Serializable {

    private static final long serialVersionUID = 1L;

            @TableId("IDsat")
    private Integer IDsat;

        @TableField("SSID")
    private String ssid;

        @TableField("TID")
    private String tid;

        @TableField("Xres")
    private String Xres;

        @TableField("CK")
    private String ck;

        @TableField("Token")
    private String Token;


}
