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
    public class PreLeo implements Serializable {

    private static final long serialVersionUID = 1L;

            @TableId("IDsat")
    private Integer IDsat;

        @TableField("SSID")
    private String ssid;

        @TableField("DKenc")
    private String DKenc;

        @TableField("DKauth")
    private String DKauth;

        @TableField("WKenc")
    private String WKenc;

        @TableField("K")
    private String k;

        @TableField("MainKey")
    private String MainKey;

        @TableField("AuthKey")
    private String AuthKey;


}
