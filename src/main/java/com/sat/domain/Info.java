package com.sat.domain;

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
public class Info implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value= "IDsat",type = IdType.INPUT)
    private String IDsat;

    @TableField("ip")
    private String ip;

    @TableField("port")
    private Integer port;



}
