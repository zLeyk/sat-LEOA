package com.sat.satquery.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sat.satquery.entity.Leoleo;
import com.sat.satquery.service.ILeoleoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Archie
 * @since 2023-08-15
 */
@CrossOrigin
@RestController
@RequestMapping("/satquery/leoleo")
public class LeoleoController {

    @Autowired
    ILeoleoService iLeoleoService;

    @GetMapping("/getAllLeoLeoInfo")
    public List<Leoleo> getAllLeoLeoInfo() {

        return iLeoleoService.list();
    }

    @GetMapping("/getLeoLeoInfoByPage")
    public PageInfo<Leoleo> getLeoLeoInfoByPage(@RequestParam(defaultValue = "1") int curPage, @RequestParam(defaultValue = "15") int pageSize) {
        PageHelper.startPage(curPage, pageSize);
        return new PageInfo<>(iLeoleoService.list());
    }

    @DeleteMapping("/deleteLeoLeoByIDsat/{idsat}")
    public boolean deleteLeoLeoByIDsat(@PathVariable int idsat) {
        return iLeoleoService.removeById(idsat);
    }

    @PutMapping("/updateLeoLeo")
    public boolean updateLeoLeo(@RequestBody Leoleo newInfo) {
        System.out.println(newInfo);
        return iLeoleoService.updateById(newInfo);
    }
}
