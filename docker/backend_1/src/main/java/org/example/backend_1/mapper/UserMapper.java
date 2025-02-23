package org.example.backend_1.mapper;

import org.apache.ibatis.annotations.*;
import org.example.backend_1.pojo.User;

@Mapper
public interface UserMapper {
    @Select("Select * FROM 苍穹杯.users where username=#{username} and password=#{password};")
    User getByUsernameAndPassword(User user);

    @Insert("INSERT 苍穹杯.users (username, password,phone) value (#{username},#{password},#{phone});")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void addNewUser(User user);

    @Update("update 苍穹杯.users set hometown=#{hometown}, interested_places=#{interestedPlaces} ,interested_ways=#{interestedWays} , travel_companion=#{travelCompanion},age=#{age},gender=#{gender} where username=#{username} and password=#{password} and id=#{id};")
    void addPreference(User user);

    @Select("SELECT * FROM 苍穹杯.users where id=#{id};")
    User getUserById(Integer id);

    @Update("update 苍穹杯.users set img_url=#{imgUrl} where id=#{id};")
    void updateInfoById(Integer id, String imgUrl);

    @Select("SELECT * FROM 苍穹杯.users where id=#{id};")
    User getById(Integer id);
}
