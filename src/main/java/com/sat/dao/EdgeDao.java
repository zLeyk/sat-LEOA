package com.sat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sat.domain.Edge;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EdgeDao extends BaseMapper<Edge> {
    @Select("select u from edge")
    List<String> selectu();
    @Select("select v from edge")
    List<String> selectv();
    @Select("select COUNT(*) FROM EDGE where u = #{u} and v=#{v}")
    int selectByUV(@Param("u") String u, @Param("v") String v);
    @Insert("INSERT into EDGE VALUES(#{cnt},#{u},#{v},#{w})")
    Boolean save2(@Param("cnt") int cnt, @Param("u") String u, @Param("v") String v, @Param("w") Integer w);
}
