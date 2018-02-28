package utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import model.CollectionModel;
import model.Poetry;
import model.RecordHold;
import model.User;

/**
 * Created by Lenovo on 2017/12/29.
 */

public class ParseJSONUtil {

    private static  String HAS_DATA = "Succes";

    public static List<Poetry> jsonStrToPoetryList(String str){
        List<Poetry> poetryList = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(str);
            if (HAS_DATA.equals(obj.getString("reason"))) {
                JSONArray arr = obj.getJSONArray("result");
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    Poetry p = new Poetry();
                    p.setId(o.getString("id"));
                    p.setTitle(o.getString("name"));
                    poetryList.add(p);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return poetryList;
    }

    public static Poetry jsonStrToPoetry(String str){
        Poetry p = new Poetry();
        try {
            JSONObject obj = new JSONObject(str);
            String reason = obj.getString("reason");
            if (HAS_DATA.equals(reason)) {
                obj = obj.getJSONObject("result");
                p.setId(obj.getString("id"));
                p.setTitle(obj.getString("biaoti"));
                p.setAuthor(obj.getString("zuozhe"));
                String zhushi = obj.getString("jieshao").replace("/r/n","");
                if (zhushi.indexOf("：") <= 0) {
                    zhushi += "\n\n此诗暂无注释";
                }
                p.setNotes(zhushi);
                String content = obj.getString("neirong");
                p.setContent(content.substring(content.indexOf("】")+1,content.length()).split("/r/n"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static User jsonStrToUser(String loginRes) {
        User u = new User();
        try {
            JSONObject obj = new JSONObject(loginRes);
            u.setPhoneNum(obj.getString("phoneNumber"));
            u.setName(obj.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return u;
    }

    public static List<RecordHold> jsonStrToRecordList(String str) {
        List<RecordHold> recordList = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(str);
                JSONArray arr = obj.getJSONArray("data");
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    RecordHold r = new RecordHold();
                    r.setId(o.getInt("id"));
                    r.setName(o.getString("name"));
                    r.setUploadTime(o.getString("uploadTime"));
                    r.setRecordPath(o.getString("recordPath"));
                    r.setPlayCount(o.getInt("playCount"));
                    r.setLike(o.getInt("praiseCount"));
                    recordList.add(r);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return recordList;
    }

    public static List<CollectionModel> jsonStrToCollectionList(String str) {
        List<CollectionModel> list = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(str);
            JSONArray arr = obj.getJSONArray("data");
            for(int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                CollectionModel cm = new CollectionModel();
                cm.setId(o.getInt("id"));
                cm.setCollectTime(o.getString("collectTime"));
                cm.setPoetryTitle(o.getString("poetryTitle"));
                cm.setPoetryId(o.getString("poetryId"));
                list.add(cm);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<RecordHold> jsonStrToMyRecordList(String str) {
        List<RecordHold> recordList = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(str);
            JSONArray arr = obj.getJSONArray("data");
            for(int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                RecordHold r = new RecordHold();
                r.setId(o.getInt("id"));
                r.setPoetryTitle(o.getString("poetryTitle"));
                r.setUploadTime(o.getString("uploadTime"));
                r.setRecordPath(o.getString("recordPath"));
                r.setPlayCount(o.getInt("playCount"));
                r.setLike(o.getInt("praiseCount"));
                recordList.add(r);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return recordList;
    }
}
