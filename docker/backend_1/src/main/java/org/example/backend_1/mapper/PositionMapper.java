package org.example.backend_1.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.backend_1.pojo.Poi;
import org.example.backend_1.pojo.Position;

import java.util.ArrayList;

@Mapper
public interface PositionMapper {

    @Select("SELECT * FROM 苍穹杯.poi where type=#{type} limit #{num};")
    ArrayList<Position> getPositionByTypeAndNum(Integer num,String type);

    @Select("SELECT * FROM 苍穹杯.poi;")
    ArrayList<Position> getPositionALL();

    @Select("SELECT * FROM 苍穹杯.poi where type=#{type};")
    ArrayList<Position> getPositionByType(String type);

    @Select("SELECT id,longitude,latitude,popularity,name FROM 苍穹杯.poi where type=#{type};")
    ArrayList<Poi> getPoiByType(String type);
}
