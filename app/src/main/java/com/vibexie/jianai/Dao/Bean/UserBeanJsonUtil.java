package com.vibexie.jianai.Dao.Bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vibexie on 5/19/15.
 */
public class UserBeanJsonUtil {

    public static JSONObject userBean2Json(UserBean userBean){
        JSONObject jsonObject=new JSONObject();

        try {
            jsonObject.put("id", userBean.getId());
            jsonObject.put("username",userBean.getUsername());
            jsonObject.put("loverName",userBean.getLoverName());
            jsonObject.put("name",userBean.getName());
            jsonObject.put("sex",userBean.getSex());
            jsonObject.put("birthday",userBean.getBirthday());
            jsonObject.put("email",userBean.getEmail());
            jsonObject.put("phone",userBean.getPhone());
            jsonObject.put("addr",userBean.getAddr());
            jsonObject.put("sign",userBean.getSign());
            jsonObject.put("creationDate",userBean.getCreationDate());
            jsonObject.put("modificationDate",userBean.getModificationDate());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static UserBean Json2UserBean(JSONObject jsonObject){
        UserBean userBean=new UserBean();

        try {
            userBean.setId(jsonObject.getInt("id"));
            userBean.setUsername(jsonObject.getString("username"));
            //userBean.setLoverName(jsonObject.getString("loverName"));
            userBean.setName(jsonObject.getString("name"));
            userBean.setSex(jsonObject.getString("sex"));
            userBean.setBirthday(jsonObject.getString("birthday"));
            userBean.setEmail(jsonObject.getString("email"));
            userBean.setPhone(jsonObject.getString("phone"));
            userBean.setAddr(jsonObject.getString("addr"));
            //userBean.setSign(jsonObject.getString("sign"));
            userBean.setCreationDate(jsonObject.getString("creationDate"));
            //userBean.setModificationDate(jsonObject.getString("modificationDate"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userBean;
    }
}
