package com.sat.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sat.domain.Edge;
import com.sat.domain.Info;
import com.sat.service.EdgeService;
import com.sat.service.InfoService;
import com.sat.utils.R;
import com.sat.utils.SendTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.*;

@RestController
@RequestMapping("/edges")
public class EdgeController {
    @Autowired
    private  EdgeService edgeService;
    private int cnt = 20;
    private static List<Edge> list = new ArrayList<Edge>();  //存放所有边信息
    private static HashMap<String, Integer> minmap = new HashMap<>(); // 存放最短路径
    private static HashSet<String> set = new HashSet<>();  //存放所有节点信息
    private static HashMap<String, String> pre = new HashMap<>();
    private static List<String> l; // 存放所有节点信息
    private static String start;  //起点
    private static String end;    //终点
    private static ArrayList<String> arrayList = new ArrayList<>();  //存放节点 最短路径的前驱节点

    //测试
    @Autowired
    public InfoService infoService;

    @GetMapping("")
    public String getx() throws IOException {
        List<Info> infoList= infoService.selectInfo();
        for (int i = 0; i < infoList.size(); i++) {
            //这部分是从数据库读取的，如果使用这部分需要先把TCC和LEO都运行，否则会报错

            System.out.println(infoList.get(i).getIp()+":"+infoList.get(i).getPort());
        }

        //测试-建立Map存储多个IP和端口号
        ArrayList<Info> iplist = new ArrayList<Info>();
        Info a = new Info("192.168.10.23", "8899");
        Info b = new Info("192.168.10.23", "8897");
        //iplist.add(a); 在测试中，打开此对应程序就取消注释
        //iplist.add(b);
        //System.out.println(iplist.get(0).getIp()+":"+iplist.get(0).getPort());

        for (int i = 0; i < iplist.size(); i++) {
            //遍历，每有一个主机节点就发起一个消息。*注意要排除自身的ip和端口

            System.out.println(iplist.get(i).getIp()+":"+iplist.get(i).getPort());
            Socket client = new Socket(iplist.get(i).getIp(), Integer.parseInt(iplist.get(i).getPort()));
            //需要加一个判断，不同的消息类型调用不同的任务
            new Thread(new SendTask(client)).start();
        }




        return "fff";
    }

    @GetMapping("{currentPage}/{pageSize}")
    public R getPage(@PathVariable int currentPage, @PathVariable int pageSize){
        System.out.println("heee");
        IPage<Edge> page = edgeService.getPage(currentPage, pageSize);
        if(currentPage > page.getPages()){
            page = edgeService.getPage((int)page.getPages(), pageSize);
        }
        return new R(true, page);
    }
    //删除边
    @DeleteMapping("{id}")
    public R delete(@PathVariable Integer id){
        return new R(edgeService.removeById(id));
    }
    //保存边
    @PostMapping
    public R save(@RequestBody Edge edge){
        this.clear();
        String u = edge.getU();
        String v = edge.getV();
        int c1 = edgeService.getByUV(u,v);
        cnt++;
//        不存在此边，添加
        if(c1==0){
            Boolean flag = edgeService.save2(cnt,edge.getU(),edge.getV(),edge.getW());
            return new R(true,"添加成功");
        }else{
            return new R(false,"存在此边");
        }
    }
    //根据起点，终点求最短路径
    @GetMapping("{u}/{v}/1")
    public R getminp(@PathVariable String u, @PathVariable String v){
        start = u;
        end = v;
        int b = begin();
        if(b == 0)
            return new R(false,null, 0);
        if(b == Integer.MAX_VALUE)
            return new R(false,null,0);
        return new R(true, arrayList,minmap.get(end));
    }
    public int begin() {
        int n,m;
        this.clear();
        list = edgeService.list();  //获得所有边信息
        //set存放所有的节点
        set = new HashSet();
        List<String> l1 = edgeService.selectu();
        List<String> l2 = edgeService.selectv();
        for(int i = 0; i < l1.size(); i++){
            set.add(l1.get(i));
        }
        for(int i = 0; i < l2.size(); i++){
            set.add(l2.get(i));
        }
         l = new ArrayList<>(set);  // 将所有边信息放在list里
        if(!set.contains(start)||!set.contains(end))
            return 0;      //图中不存在此节点 返回0
        n = set.size(); // n为节点数
        m = list.size();  //边数
        int f = dij(n,m);
//        如果存在最短路径  将最短路径保存在list里
        if(f != Integer.MAX_VALUE) {
            String p = pre.get(end); //终点最短路径的前驱节点
            arrayList.add(end);
            while (p != start) {
                arrayList.add(p);
                p = pre.get(p);
            }
            arrayList.add(start);
            Collections.reverse(arrayList);  //逆置
        }
        return f;
    }

    int dij(int n, int m){
//        初始化最短距离数组
        for(int i = 0; i < l.size(); i++) {
            String a = l.get(i);
            minmap.put(a, Integer.MAX_VALUE);
        }
        for(int j = 0; j < m; j++){
                Edge e = list.get(j);
                if(e.getU().equals(start)){
                    minmap.put(e.getV(),e.getW());
                    pre.put(e.getV(),start);
                }
        }
        ArrayList<String> al = new ArrayList<>(minmap.keySet());  //获取所有节点
          //去除起始节点
        for(int i = 0 ; i < al.size(); i++){
            if(start.equals(al.get(i))){
                al.remove(i);
                break;
            }
        }
        //算n-1个最短路径
        for(int i = 0; i <n-1; i++){
            Integer min = Integer.MAX_VALUE;
            String  smin = null;
            for(int j = 0; j < al.size(); j++){
                String a = al.get(j);
                Integer mm = minmap.get(a);
                if(mm <= min){
                    min = mm;
                    smin = a;
                }
            }
            minmap.put(smin, min);
            if(smin.equals(end))
                break;
//             去除获得最段路径的节点
            for(int j = 0; j < al.size(); j++){
                if(smin.equals(al.get(j))){
                    al.remove(j);
                    break;
                }
            }
//            更新minmap表
            for(int j = 0; j < list.size(); j++){
                Edge e = list.get(j);
                if(e.getU().equals(smin)){
                    if(minmap.get(smin)+e.getW()<minmap.get(e.getV())){
                        minmap.put(e.getV(),minmap.get(smin)+e.getW());
                        pre.put(e.getV(),smin);
                    }
                }
            }
        }
        return minmap.get(end);
    }
     void clear(){
        arrayList.clear();
        list.clear();
        set.clear();
        minmap.clear();

    }
}
