package com.sat.satquery.controller;


import com.sat.satquery.entity.Preleo;
import com.sat.satquery.service.IPreleoService;
import com.sat.utils.LeoTccAuthTask;
import com.sat.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
@CrossOrigin
@RestController
@RequestMapping("/satquery/tccleo")
public class TccleoController {

    @Autowired
    IPreleoService iPreleoService;


    @GetMapping("/auth")
    public R Auth() throws IOException {
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
        R r = new R();
        if (t.getSt() == "认证成功"){
            r.setFlag(true);
        }else {
            r.setFlag(false);
        }
        r.setData(list.get(0));
        r.setLog(t.getLog());
        return r;
    }

}
