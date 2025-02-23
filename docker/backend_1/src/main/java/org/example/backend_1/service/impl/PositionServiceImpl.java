package org.example.backend_1.service.impl;

import org.example.backend_1.mapper.PositionMapper;
import org.example.backend_1.pojo.Poi;
import org.example.backend_1.pojo.Position;
import org.example.backend_1.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PositionServiceImpl implements PositionService {
    @Autowired
    PositionMapper positionMapper;
    @Override
    public ArrayList<Position> getPosition(Integer num,String type) {
        return positionMapper.getPositionByTypeAndNum(num,type);
    }

    @Override
    public ArrayList<Position> getALLPosition() {
        return positionMapper.getPositionALL();
    }

    @Override
    public ArrayList<Position> getPositionByType(String type) {
        return positionMapper.getPositionByType(type);
    }

    @Override
    public ArrayList<Poi> getPoiByType(String type) {
        return positionMapper.getPoiByType(type);
    }
}
