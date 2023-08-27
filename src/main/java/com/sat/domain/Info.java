package com.sat.domain;

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
    @TableId("IDsat")
    private String IDsat;
    private String ip;

    private Integer port;


}
