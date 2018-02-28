package model;

import android.app.Application;

/**
 * Created by Administrator on 2018/1/11.
 */

public class MyApplication extends Application {
    private String phoneNumber;
    private User user;
    private Poetry currPoetry;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setUser(User u) {
        user = u;
    }
    public User getUser(){
        return this.user;
    }

    public Poetry getCurrPoetry() {
        return currPoetry;
    }

    public void setCurrPoetry(Poetry currPoetry) {
        this.currPoetry = currPoetry;
    }
}
