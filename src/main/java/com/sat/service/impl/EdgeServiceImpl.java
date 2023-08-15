package com.sat.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sat.dao.EdgeDao;
import com.sat.domain.Edge;
import com.sat.service.EdgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EdgeServiceImpl extends ServiceImpl<EdgeDao, Edge> implements EdgeService {
   @Autowired
   private EdgeDao edgeDao;
    @Override
    public List<String> selectu() {
        return edgeDao.selectu();
    }

    @Override
    public List<String> selectv() {
        return edgeDao.selectv();
    }

    @Override
    public IPage<Edge> getPage(int currentPage, int pageSize) {
        IPage page = new Page(currentPage,pageSize);
        edgeDao.selectPage(page,null);
        return page;
    }

    @Override
    public int getByUV(String u, String v) {
        return edgeDao.selectByUV(u,v);
    }

    @Override
    public Boolean save2(int cnt, String u, String v, Integer w) {
        return edgeDao.save2(cnt, u,v,w);
    }
}
