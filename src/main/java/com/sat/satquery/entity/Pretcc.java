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
    public class Pretcc implements Serializable {

    private static final long serialVersionUID = 1L;

            @TableId("ID")
    private Integer id;

        @TableField("DKauth")
    private String DKauth;

        @TableField("DKenc")
    private String DKenc;

        @TableField("Wkenc")
    private String Wkenc;

        @TableField("K")
    private String k;


}
