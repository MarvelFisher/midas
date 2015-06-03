package com.cyanspring.common.account;

import java.io.Serializable;

public class ThirdPartyUser implements Serializable {

    private String id;

    private UserType userType;

    public ThirdPartyUser(String id, UserType userType) {
        this.id = id;
        this.userType = userType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "ThirdPartyUser{" +
                "id='" + id + '\'' +
                ", userType=" + userType +
                '}';
    }
}
