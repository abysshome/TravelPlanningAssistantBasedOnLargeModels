package org.example.backend_1.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddUserReturn {
    private Integer id;
    private String personalPreference;
    private String hometown;
    private String interestedPlaces;
    private String interestedWays;
    private String travelCompanion;
    private String imgUrl;
    public AddUserReturn(User user){
        this.id=user.getId();
        this.personalPreference=user.getPersonalPreference();
        this.hometown=user.getHometown();
        this.interestedPlaces=user.getInterestedPlaces();
        this.interestedWays=user.getInterestedWays();
        this.travelCompanion=user.getTravelCompanion();
    }
}
