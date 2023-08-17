package com.sat.satquery.controller;


import com.sat.satquery.entity.Preleo;
import com.sat.satquery.service.IPreleoService;
import com.sat.utils.LeoTccAuthTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.Socket;
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

    @RequestMapping("/auth")
    public String Auth() throws IOException {
        Socket socket = new Socket("127.0.0.1",8899);
        List<Preleo> list = iPreleoService.list();
        LeoTccAuthTask t = new LeoTccAuthTask(socket,list.get(0));
        Thread th  =new Thread(t);
        th.start();
        //返回认证结果
        try {
            th.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return t.getSt();
    }

}