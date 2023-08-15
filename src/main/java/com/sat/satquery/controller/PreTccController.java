package com.sat.satquery.controller;


import com.sat.satquery.entity.PreTcc;
import com.sat.satquery.service.IPreTccService;
import com.sat.satquery.service.ITccLeoAuthenticationService;
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
 * @since 2023-08-14
 */
@RestController
@RequestMapping("/satquery/pre-tcc")
public class PreTccController {
    @Autowired
    IPreTccService iPreTccService;

    @RequestMapping("/getAllPreTCCInfo")
    public List<PreTcc> getAllPreTCCInfo() {
        return iPreTccService.list();
    }
}
