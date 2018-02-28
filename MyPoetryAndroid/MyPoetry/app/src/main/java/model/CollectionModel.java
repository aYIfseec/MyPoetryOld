package model;

/**
 * Created by Administrator on 2018/1/12.
 */

public class CollectionModel {
    private int id;
    private String phoneNumber;
    private String poetryId;
    private String poetryTitle;
    private String collectTime;

    public String getPoetryTitle() {
        return poetryTitle;
    }
    public void setPoetryTitle(String poetryTitle) {
        this.poetryTitle = poetryTitle;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getPoetryId() {
        return poetryId;
    }
    public void setPoetryId(String poetryId) {
        this.poetryId = poetryId;
    }
    public String getCollectTime() {
        return collectTime;
    }
    public void setCollectTime(String collectTime) {
        this.collectTime = collectTime;
    }
}
