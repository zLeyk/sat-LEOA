package com.sat.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sat.dao.InfoDao;
import com.sat.domain.Info;
import com.sat.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@DS("hostinfo")
public class InfoServiceImpl extends ServiceImpl<InfoDao, Info> implements InfoService {

    @Autowired
    private InfoDao infoDao;

    @Override
    public List<Info> selectInfo() {
        return infoDao.selectInfo();
    }
}
