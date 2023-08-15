package com.sat.satquery.controller;


import com.sat.satquery.entity.Leoleo;
import com.sat.satquery.service.ILeoleoService;
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
@RequestMapping("/satquery/leoleo")
public class LeoleoController {

    @Autowired
    ILeoleoService iLeoleoService;

    @RequestMapping("/getAllLeoLeoInfo")
    public List<Leoleo> getAllLeoLeoInfo() {
        return iLeoleoService.list();
    }

}
