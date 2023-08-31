package com.sat.satquery.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

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
    public ArrayList<Leoleo> broadcastInfo(@RequestBody ArrayList<Info> list) {
        //调用Serice查询B的因为后面需要预置信息
        List<Preleo> list1 = iPreleoService.list();
        //遍历所有与A相连的卫星
        int cnt = list.size();
        CountDownLatch latch = new CountDownLatch(cnt);

        for(Info info: list) {
            boolean flag = true;
            Socket socket = null;
            try {
                System.out.println("Port:"+info.getPort());
//                socket = new Socket(info.getIp(), info.getPort());
                socket = new Socket(info.getIp(), info.getPort());
            } catch (Exception e) {
                flag = false;
                String iDsat = info.getIDsat();   //之前是否存在认证状态 存在就删除，不存在
                QueryWrapper qw = new QueryWrapper<>();
                qw.eq("IDsat",iDsat);
                List<Leoleo> leoleo = iLeoleoService.list(qw);
                System.out.println("leoleo"+leoleo);
                Leoleo leoleoa = new Leoleo();
                leoleoa.setIDsat(iDsat);
                leoleoa.setSt(3);//设置状态 为3
                leoleoa.setLog("目标主机未开启\n认证失败\n");
                if(leoleo!=null){
//                    iLeoleoService.removeById(iDsat);
                    leoleoa.setToken(leoleo.get(0).getToken());
                    leoleoa.setTidSrc(leoleo.get(0).getTidSrc());
                    leoleoa.setTidDst(leoleo.get(0).getTidDst());
                    leoleoa.setSsid(leoleo.get(0).getSsid());
                    iLeoleoService.remove(qw);
                }
                System.out.println("leoleoa"+leoleoa);

                System.out.println("beforeileoleoservice"+iLeoleoService.list());
                iLeoleoService.save(leoleoa);
                System.out.println("ileoleoservice"+iLeoleoService.list());
                latch.countDown();
            }
            if(flag) {
                LeoLeoAuthTask t = new LeoLeoAuthTask(socket, info.getIDsat(), list1.get(0), latch);
                new Thread(t).start();
            }
        }
        try {
            latch.await();  //
        } catch (InterruptedException e) {
        }


        //查询认证过程插入的星间认证信息 ，并返回List
        List<Leoleo> result = iLeoleoService.list();
        ArrayList<Leoleo> re = new ArrayList<>();
        for (Leoleo le:
                result) {
            if(!le.getLog().equals("目的卫星认证成功")) {
                re.add(le);
            }
        }
        System.out.println(re);
        return re;
    }

}
