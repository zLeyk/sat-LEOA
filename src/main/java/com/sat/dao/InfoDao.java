//package com.sat.dao;
//
//import com.baomidou.dynamic.datasource.annotation.DS;
//import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import com.sat.domain.Info;
//import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Select;
//
//import java.util.List;
//
//@Mapper
//@DS("hostinfo")
//public interface InfoDao extends BaseMapper<Info> {
//    @Select("select ip, port from info")
//    List<Info> selectInfo();
//}
