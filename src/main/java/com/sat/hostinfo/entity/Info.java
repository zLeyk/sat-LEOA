package com.sat.hostinfo.entity;

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
    public class Info implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ip;

    private String port;


}
