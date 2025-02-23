package org.example.backend_1.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Poi{
    public Integer id;
    public String name;
    public Double longitude;
    public Double latitude;
    public Double popularity;
    public Double score;

}
