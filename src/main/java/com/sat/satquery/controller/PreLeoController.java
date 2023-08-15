package com.sat.satquery.controller;


import com.sat.satquery.entity.Leoleo;
import com.sat.satquery.entity.Preleo;
import com.sat.satquery.service.ILeoleoService;
import com.sat.satquery.service.IPreleoService;
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
@RequestMapping("/satquery/preleo")
public class PreleoController {

    @Autowired
    IPreleoService iPreleoService;

    @RequestMapping("/getAllPreLeoInfo")
    public List<Preleo> getAllPreLeoInfo() {
        return iPreleoService.list();
    }

}
