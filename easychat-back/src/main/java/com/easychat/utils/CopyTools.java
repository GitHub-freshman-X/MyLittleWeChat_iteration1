package com.easychat.utils;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;

public class CopyTools {

    public static <T, S> List<T> copyList(List<S> sList, Class<T> classT) {
        List<T> list = new ArrayList<>();
        for (S s : sList) {
            T t = null;
            try {
                t = classT.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s, t);
            list.add(t);
        }
        return list;
    }

    public static <T, S> T copy(S s, Class<T> classZ) {
        T t = null;
        try {
            t = classZ.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BeanUtils.copyProperties(s, t);
        return t;
    }
}
