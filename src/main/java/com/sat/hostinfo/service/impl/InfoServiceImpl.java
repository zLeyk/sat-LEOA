package com.sat.hostinfo.service.impl;

import com.sat.hostinfo.entity.Info;
import com.sat.hostinfo.mapper.InfoMapper;
import com.sat.hostinfo.service.IInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Archie
 * @since 2023-08-15
 */
@Service
public class InfoServiceImpl extends ServiceImpl<InfoMapper, Info> implements IInfoService {

}
