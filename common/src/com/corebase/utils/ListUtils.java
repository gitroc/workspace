package com.corebase.utils;

import java.util.List;

public class ListUtils<T>{

    /**
     * 获取两列表的交集
     * @param list1
     * @param list2
     * @return
     */
    public List<T> getListInterSection(List<T> list1, List<T> list2){
        list1.retainAll(list2);
        return list1;
    }
    
    /**
     * 获取两列表的并集
     * @param list1
     * @param list2
     * @return
     */
    public List<T> getListUnion(List<T> list1, List<T> list2){
        for(T t:list1){
            if(!list2.contains(t)){
                list2.add(t);
            }
        }
        return list2;
    }
    
    /**
     * 判断俩个列表元素是否相等
     * @param list1
     * @param list2
     * @return
     */
    public boolean isListEquals(List<T> list1, List<T> list2){
        if(list1==null&&list2==null){
            return true;
        }else if(list1==null||list2==null){
            return false;
        }
        return list1.containsAll(list2)&&list2.containsAll(list1);
    }
}
