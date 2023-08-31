package com.sat.satquery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
public class Leoleo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "IDsat", type = IdType.INPUT)
    private String IDsat;

    @TableField("SSID")
    private Integer ssid;

    @TableField("TidSrc")
    private String TidSrc;

    @TableField("TidDst")
    private String TidDst;

    @TableField("ST")
    private Integer st;

    @TableField("Token")
    private String Token;

    @TableField("log")
    private String log;

}
