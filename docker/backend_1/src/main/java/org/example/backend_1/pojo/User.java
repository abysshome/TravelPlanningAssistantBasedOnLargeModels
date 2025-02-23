package org.example.backend_1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String username;
    private String password;
    private String phone;
    private String personalPreference;
    private String hometown;
    private String interestedPlaces;
    private String interestedWays;
    private String travelCompanion;
    private String imgUrl;
    private String gender;
    private Integer age;
}
