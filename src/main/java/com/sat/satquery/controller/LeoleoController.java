package com.sat.satquery.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sat.domain.Info;
import com.sat.satquery.entity.Leoleo;
import com.sat.satquery.entity.Preleo;
import com.sat.satquery.service.ILeoleoService;
import com.sat.satquery.service.IPreleoService;
import com.sat.utils.LeoLeoAuthTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.sound.sampled.Line;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
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

    @Autowired
    IPreleoService iPreleoService;


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

    //星间认证方法
    @PostMapping("/leoAu")
    public ArrayList<Leoleo> broadcastInfo(@RequestBody ArrayList<Info> list)throws Exception {
        //调用Serice查询B的因为后面需要预置信息
        List<Preleo> list1 = iPreleoService.list();
        //遍历所有与A相连的卫星
        for(Info info: list) {
            Socket socket = new Socket(info.getIp(), info.getPort());
            LeoLeoAuthTask t = new LeoLeoAuthTask(socket,info.getIDsat(),list1.get(0));
            new Thread(t).start();
        }
        Thread.sleep(5000);
        //查询认证过程插入的星间认证信息 ，并返回List
        List<Leoleo> result = iLeoleoService.list();
        ArrayList<Leoleo> re = new ArrayList<>();
        for (Leoleo le:
             result) {
            re.add(le);
        }
        System.out.println(re);
        return re;
    }

}
