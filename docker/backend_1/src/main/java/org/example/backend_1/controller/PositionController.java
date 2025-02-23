package org.example.backend_1.controller;

import org.example.backend_1.pojo.Poi;
import org.example.backend_1.pojo.PoiOutput;
import org.example.backend_1.pojo.Position;
import org.example.backend_1.pojo.Result;
import org.example.backend_1.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.example.backend_1.utils.SelectPOI;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/position")
public class PositionController {
    @Autowired
    private PositionService positionService;
    private static final ArrayList<ArrayList<Poi>> pois = new ArrayList<>();
    @PostConstruct
    public void init() {
        HashMap<String, Double> weights = new HashMap<>();
        weights.put("attraction", 0.6);
        weights.put("leisure", 0.1);
        weights.put("shopping", 0.1);
        weights.put("hotel", 0.1);
        weights.put("restaurant", 0.1);
        Iterator<Map.Entry<String, Double>> iterator = weights.entrySet().iterator();
        ArrayList<Poi> res = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            pois.add(positionService.getPoiByType(entry.getKey()));
        }
    }
    @GetMapping("")
    public Result getPosition(Integer num, String type) {
        System.out.println(num);
        ArrayList<Position> points = positionService.getPosition(num, type);
        if (points == null) {
            return Result.error("获取poi失败");
        } else {
            return Result.success(points);
        }
    }
    @GetMapping("filter")
    public Result filterPOI_1(String begin, String end, Double num) {
        HashMap<String, Double> weights = new HashMap<>();
        weights.put("attraction", 0.6);
        weights.put("leisure", 0.1);
        weights.put("shopping", 0.1);
        weights.put("hotel", 0.1);
        weights.put("restaurant", 0.1);
        Iterator<Map.Entry<String, Double>> iterator = weights.entrySet().iterator();
        ArrayList<Poi> res = new ArrayList<>();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry<String, Double> entry = iterator.next();
            double n = entry.getValue() * num;
            System.out.println(entry.getKey()+":"+n);
            res.addAll(SelectPOI.Select_1(pois.get(i++), SelectPOI.getLL(begin), SelectPOI.getLL(end), (int) n));
        }
        ArrayList<PoiOutput>  output=new ArrayList<>();
        for (Poi poi : res) {
            output.add(new PoiOutput(poi.name,poi.longitude,poi.latitude));
        }
        System.out.println(output.size());
        return Result.success(output);
    }
}
