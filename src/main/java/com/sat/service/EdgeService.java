package com.sat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sat.domain.Edge;

import java.util.List;

public interface EdgeService extends IService<Edge> {
    List<String> selectu();
    List<String> selectv();
    IPage<Edge> getPage(int currentPage, int pageSize);

    int getByUV(String u, String v);

    Boolean save2(int cnt, String u, String v, Integer w);
}
