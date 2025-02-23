package org.example.backend_1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteData {
    private String 出发地点;
    private String 出发时间;
    private List<PointOfInterest> 途径景点;
    private String 结束地点;
    private String 结束时间;
    private Location 出发地点经纬度;
    private Location 结束地点经纬度;
}