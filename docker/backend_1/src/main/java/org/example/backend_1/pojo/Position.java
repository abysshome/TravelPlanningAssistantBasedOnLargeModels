package org.example.backend_1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private Integer id;
    private String name;
    private String largeCategory;
    private String middleCategory;
    private String smallCategory;
    private String address;
    private String province;
    private String city;
    private String district;
    private Double longitude;
    private Double latitude;
    private String type;
    private Double popularity;
    private Double score;

    public Position(Double longitude,Double latitude,String name){
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
    }
}
