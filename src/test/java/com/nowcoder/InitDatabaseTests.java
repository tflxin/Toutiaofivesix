package com.nowcoder;

import com.nowcoder.dao.CommentDAO;

import com.nowcoder.dao.LoginTicketDAO;

import com.nowcoder.dao.NewsDAO;

import com.nowcoder.dao.UserDAO;

import com.nowcoder.model.*;

import org.junit.Assert;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.SpringApplicationConfiguration;

import org.springframework.test.context.jdbc.Sql;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import java.util.Date;

import java.util.Random;


@RunWith(SpringJUnit4ClassRunner.class)

@SpringApplicationConfiguration(classes = ToutiaoApplication.class)

@Sql("/init-schema.sql")

public class InitDatabaseTests {
   
 @Autowired
   
 UserDAO userDAO;

   
 @Autowired
   
 NewsDAO newsDAO;

  
  @Autowired
   
 LoginTicketDAO loginTicketDAO;

   
 @Autowired
    CommentDAO commentDAO;

 
   @Test
  
  public void initData() {
   
     //随机产生10个用户
       Random random = new Random();
 
       for (int i = 0; i < 11; ++i) {
       
        User user = new User();
    
        user.setHeadUrl(String.format("http://images.nowcoder.com/head/%dt.png", random.nextInt(1000)));
  
          user.setName(String.format("USER%d", i));
    
        user.setPassword("");
     
       user.setSalt("");
    
        userDAO.addUser(user);


        News news = new News();

        news.setCommentCount(i);
       
        Date date = new Date();
     
       date.setTime(date.getTime() + 1000*3600*5*i);
       
       news.setCreatedDate(date);
         
    news.setImage(String.format("http://images.nowcoder.com/head/%dm.png", random.nextInt(1000)));
   
         news.setLikeCount(i+1);
        
    news.setUserId(i+1);
      
      news.setTitle(String.format("TITLE{%d}", i));
     
       news.setLink(String.format("http://www.nowcoder.com/%d.html", i));
   
         newsDAO.addNews(news);

         
   // 给每个资讯插入3个评论
         
   for(int j = 0; j < 3; ++j) {
                Comment comment = new Comment();
               //i+1 用户的id
                comment.setUserId(i+1);
                comment.setCreatedDate(new Date());
                comment.setStatus(0);
                //评论
                comment.setContent("这里是一个评论啊！" + String.valueOf(j));
                //新闻的id
                comment.setEntityId(news.getId());
                comment.setEntityType(EntityType.ENTITY_NEWS);
                commentDAO.addComment(comment);
            }

            user.setPassword("newpassword");
            userDAO.updatePassword(user);

            LoginTicket ticket = new LoginTicket();
            ticket.setStatus(0);
            ticket.setUserId(i+1);
            ticket.setExpired(date);
            ticket.setTicket(String.format("TICKET%d", i+1));
            loginTicketDAO.addTicket(ticket);

            loginTicketDAO.updateStatus(ticket.getTicket(), 2);

        }

        //断言调试：debug崩溃但是编译时候自动忽略
        Assert.assertEquals("newpassword", userDAO.selectById(1).getPassword());
        userDAO.deleteById(1);
        Assert.assertNull(userDAO.selectById(1));

        Assert.assertEquals(1, loginTicketDAO.selectByTicket("TICKET1").getUserId());
        Assert.assertEquals(2, loginTicketDAO.selectByTicket("TICKET1").getStatus());

        //为了保证评论已经插进去了，把新闻选出来：这里取第一条，保证不为空
        Assert.assertNotNull(commentDAO.selectByEntity(1, EntityType.ENTITY_NEWS).get(0));
    }

}
