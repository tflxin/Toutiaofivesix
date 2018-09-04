package com.nowcoder.dao;

import com.nowcoder.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by nowcoder on 2018/7/2.
 * 评论的底层操作
 * addComment；selectByEntity；getCommentCount
 * void updateStatus：删除一条评论：其实就是更新；没有返回值
 */
@Mapper
public interface CommentDAO {
    String TABLE_NAME = " comment ";
    String INSERT_FIELDS = " user_id, content, created_date, entity_id, entity_type, status ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    //分页实现：就是加一个offsert limit
    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{userId},#{content},#{createdDate},#{entityId},#{entityType},#{status})"})
    int addComment(Comment comment);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where entity_type=#{entityType} " +
            "and entity_id=#{entityId} order by id desc "})
    List<Comment> selectByEntity(@Param("entityId") int entityId, @Param("entityType") int entityType);

    @Select({"select count(id) from ", TABLE_NAME, " where entity_type=#{entityType} and entity_id=#{entityId}"})
    int getCommentCount(@Param("entityId") int entityId, @Param("entityType") int entityType);
     //删除一条评论：其实就是更新
    @Update({"update", TABLE_NAME, "set status=#{status} where entity_type=#{entityType} and entity_id=#{entityId}"})
    void updateStatus(@Param("entityId") int  entityId, @Param("entityType") int entityType,
                      @Param("status") int status);
}














