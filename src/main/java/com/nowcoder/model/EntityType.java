package com.nowcoder.model;

/**
 * Created by nowcoder on 2018/7/7.
 面向接口还是面向实体
 本意是将评论和新闻关联起来，但是评论不只是在新闻下有，别人的评论下也有，所以字段要
 设计为实体类，entity_id,entity_type;约定entity_type中1 为news，2 为comment；



 做一个枚举，或者静态的类
 */
public class EntityType {
    public static int ENTITY_NEWS = 1;
    public static int ENTITY_COMMENT = 1;
}
