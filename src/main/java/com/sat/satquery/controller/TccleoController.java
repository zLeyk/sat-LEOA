package com.sat.satquery.controller;


import com.sat.satquery.entity.Pretcc;
import com.sat.satquery.entity.Tccleo;
import com.sat.satquery.service.IPretccService;
import com.sat.satquery.service.ITccleoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Archie
 * @since 2023-08-15
 */
@RestController
@RequestMapping("/satquery/tccleo")
public class TccleoController {

    @Autowired
    ITccleoService iTccleoService;

    @RequestMapping("/getAllTccLeoInfo")
    public List<Tccleo> getAllTccLeoInfo() {
        return iTccleoService.list();
    }

}
