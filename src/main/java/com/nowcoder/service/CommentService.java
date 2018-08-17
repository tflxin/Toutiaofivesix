package com.nowcoder.service;

import com.nowcoder.dao.CommentDAO;
import com.nowcoder.model.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by nowcoder on 2018/7/7.
 评论中心是所有的实体类的评论，只写一个newId则不能再对其进行使用：
 给所有的业务进行评论：随时对评论进行更改。属于哪一个id，评论的内容，评论的实体：通过两个字段（newId，commentId）的组合，
 可以代表所有的：如可代表新闻的评论，对评论进行评论
 */
@Service
public class CommentService {
    private static final Logger logger = LoggerFactory.getLogger(QiniuService.class);

    @Autowired
    CommentDAO commentDAO;
     public void deleteComment(int entityId,int entityType){
         commentDAO.updateStatus(entityType,entityId,1);
     }

    public List<Comment> getCommentsByEntity(int entityId, int entityType) {
        return commentDAO.selectByEntity(entityId, entityType);
    }

    public int addComment(Comment comment) {
         return commentDAO.addComment(comment);
    }

    public int getCommentCount(int entityId, int entityType) {
        return commentDAO.getCommentCount(entityId, entityType);
    }
}
















