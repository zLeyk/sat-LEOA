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


}
