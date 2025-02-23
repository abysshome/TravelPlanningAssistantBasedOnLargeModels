package org.example.backend_1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointOfInterest {
        private String 到达时间;
        private String 名称;
        private String 离开时间;
        private double longitude;
        private double latitude;
}
