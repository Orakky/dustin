package com.dustin.ai.support.utils;

import com.dustin.ai.support.enums.BaseEnums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnumUtils {

    /**
     * 判断枚举值是否在枚举类里
     * @param enums
     * @param value
     * @return
     */
    public static boolean isExist(BaseEnums[] enums, String value){
        if(null == value){
            return false;
        }

        for (BaseEnums e : enums) {
            if(value.equals(e.getValue())){
                return true;
            }
        }

        return false;
    }

    public static String getNameByValue(BaseEnums[] enums,String value){
        if(value.isEmpty()){
            return "";
        }

        for (BaseEnums e : enums) {
            if(value.equals(e.getValue())){
                return e.getName();
            }

        }
        return "";
    }

    public static String getNameByCode(BaseEnums[] enums, String code){
        if(code.isEmpty()){
            return "";
        }

        for (BaseEnums e : enums) {
            if(code.equals(e.getCode())){
                return e.getName();
            }
        }

        return "";
    }

    public static String getCodeByName(BaseEnums[] enums,String name){
        if(name.isEmpty()){
            return "";
        }

        for (BaseEnums e : enums) {
            if(name.equals(e.getName())){
                return e.getCode();
            }
        }
        return "";
    }
    public static String getValueByCode(BaseEnums[] enums,String code){
        if(code.isEmpty()){
            return "";
        }
        for (BaseEnums e : enums) {
            if(code.equals(e.getCode())){
                return e.getValue();
            }
        }
        return "";
    }
    public static List<Map<String,String>> getMapListByEnums(BaseEnums[] enums) {
        List<Map<String,String>> list = new ArrayList<>();
        for (BaseEnums e : enums) {
            list.add(getMapByEnums(e));

        }
        return list;
    }

    public static Map<String,String> getMapByEnums(BaseEnums enums) {
        Map<String,String> map = new HashMap<>();
        map.put("code",enums.getCode());
        map.put("name",enums.getName());
        map.put("value",enums.getValue());
        return map;
    }
}
