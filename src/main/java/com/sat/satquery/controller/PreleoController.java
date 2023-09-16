package com.sat.satquery.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sat.satquery.entity.Leoleo;
import com.sat.satquery.entity.Preleo;
import com.sat.satquery.service.IPreleoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/satquery/preleo")
public class PreleoController {

    @Autowired
    IPreleoService iPreleoService;

    @GetMapping("/getPreLeoInfoByPage")
    public PageInfo<Preleo> getPreLeoInfoByPage(@RequestParam(defaultValue = "1") int curPage, @RequestParam(defaultValue = "15") int pageSize) {
        PageHelper.startPage(curPage, pageSize);
        return new PageInfo<>(iPreleoService.list());
    }

    @DeleteMapping("/deletePreLeoByIDsat/{idsat}")
    public boolean deletePreLeoByIDsat(@PathVariable String idsat) {
        return iPreleoService.removeById(idsat);
    }

    @PutMapping("/updatePreLeo")
    public boolean updatePreLeo(@RequestBody Preleo newInfo) {
        return iPreleoService.updateById(newInfo);
    }

}
