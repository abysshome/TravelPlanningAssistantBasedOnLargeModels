package org.example.backend_1.service;

import org.example.backend_1.pojo.Poi;
import org.example.backend_1.pojo.Position;

import java.util.ArrayList;

public interface PositionService {
    ArrayList<Position> getPosition(Integer num,String type);

    ArrayList<Position> getALLPosition();

    ArrayList<Position> getPositionByType(String type);

    ArrayList<Poi> getPoiByType(String type);
}
