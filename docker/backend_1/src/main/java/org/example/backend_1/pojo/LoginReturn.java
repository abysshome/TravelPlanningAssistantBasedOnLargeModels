package org.example.backend_1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginReturn extends User {
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
    private String token;
    public LoginReturn(User u, String token){
        this.id=u.getId();
        this.username=u.getUsername();
        this.password=u.getPassword();
        this.phone=u.getPhone();
        this.personalPreference=u.getPersonalPreference();
        this.hometown=u.getHometown();
        this.interestedPlaces=u.getInterestedPlaces();
        this.interestedWays=u.getInterestedWays();
        this.travelCompanion=u.getTravelCompanion();
        this.imgUrl=u.getImgUrl();
        this.token=token;
    }
}
