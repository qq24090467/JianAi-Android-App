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
            jsonObject.put("username",userBean.getUsername()==null?"":userBean.getUsername());
            jsonObject.put("loverName",userBean.getLoverName()==null?"":userBean.getLoverName());
            jsonObject.put("encryptedPassword",userBean.getEncryptedPassword()==null?"":userBean.getEncryptedPassword());
            jsonObject.put("name",userBean.getName()==null?"":userBean.getName());
            jsonObject.put("sex",userBean.getSex()==null?"":userBean.getSex());
            jsonObject.put("birthday",userBean.getBirthday()==null?"":userBean.getBirthday());
            jsonObject.put("email",userBean.getEmail()==null?"":userBean.getEmail());
            jsonObject.put("phone",userBean.getPhone()==null?"":userBean.getPhone());
            jsonObject.put("addr",userBean.getAddr()==null?"":userBean.getAddr());
            jsonObject.put("sign",userBean.getSign()==null?"":userBean.getSign());
            jsonObject.put("creationDate",userBean.getCreationDate()==null?"":userBean.getCreationDate());
            jsonObject.put("modificationDate",userBean.getModificationDate()==null?"":userBean.getModificationDate());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static UserBean Json2UserBean(JSONObject jsonObject){
        UserBean userBean=new UserBean();

        try {
            userBean.setId(jsonObject.getInt("id"));
            userBean.setUsername(jsonObject.getString("username").equals("") ? null : jsonObject.getString("username"));
            userBean.setLoverName(jsonObject.getString("loverName").equals("") ? null : jsonObject.getString("loverName"));
            userBean.setEncryptedPassword(jsonObject.getString("encryptedPassword").equals("") ? null : jsonObject.getString("encryptedPassword"));
            userBean.setName(jsonObject.getString("name").equals("") ? null : jsonObject.getString("name"));
            userBean.setSex(jsonObject.getString("sex").equals("") ? null : jsonObject.getString("sex"));
            userBean.setBirthday(jsonObject.getString("birthday").equals("") ? null : jsonObject.getString("birthday"));
            userBean.setEmail(jsonObject.getString("email").equals("") ? null : jsonObject.getString("email"));
            userBean.setPhone(jsonObject.getString("phone").equals("") ? null : jsonObject.getString("phone"));
            userBean.setAddr(jsonObject.getString("addr").equals("") ? null : jsonObject.getString("addr"));
            userBean.setSign(jsonObject.getString("sign").equals("") ? null : jsonObject.getString("sign"));
            userBean.setCreationDate(jsonObject.getString("creationDate").equals("") ? null : jsonObject.getString("creationDate"));
            userBean.setModificationDate(jsonObject.getString("modificationDate").equals("") ? null : jsonObject.getString("modificationDate"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userBean;
    }
}
