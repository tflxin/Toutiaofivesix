package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.NewsService;
import com.nowcoder.service.QiniuService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletResponse;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by nowcoder on 2018/7/2.
 */
@Controller
public class NewsController {
    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);
    @Autowired
    NewsService newsService;

    @Autowired
    QiniuService qiniuService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;
/*
* 第六次课：评论功能
* */
   //把资讯取出来并放到detail模板上，显示在页面
    @RequestMapping(path = {"/news/{newsId}"}, method = {RequestMethod.GET})
    public String newsDetail(@PathVariable("newsId") int newsId, Model model) {
        try {
           //加上资讯
            News news = newsService.getById(newsId);
            if (news != null) {
                List<Comment> comments = commentService.getCommentsByEntity(news.getId(), EntityType.ENTITY_NEWS);
              //专门用来传递页面
                List<ViewObject> commentVOs = new ArrayList<ViewObject>();
                for (Comment comment : comments) {
                    ViewObject commentVO = new ViewObject();
                    //取出相关的用户和评论
                    commentVO.set("comment", comment);
                    commentVO.set("user", userService.getUser(comment.getUserId()));
                    commentVOs.add(commentVO);
                }
               //vo加进去model：comments
                model.addAttribute("comments", commentVOs);
            }
            //加上资讯
            model.addAttribute("news", news);
            model.addAttribute("owner", userService.getUser(news.getUserId()));
        } catch (Exception e) {
            logger.error("获取资讯明细错误" + e.getMessage());
        }
        return "detail";
    }
//展示一个图片：因为本身就是一个二进制，所以不用渲染.通过名字获取图片，并展示给前端
    @RequestMapping(path = {"/image"}, method = {RequestMethod.GET})
    @ResponseBody
    public void getImage(@RequestParam("name") String imageName,
                         HttpServletResponse response) {
        try {
            //设置返回类型
            response.setContentType("image/jpeg");
            //
            StreamUtils.copy(new FileInputStream(new
                    File(ToutiaoUtil.IMAGE_DIR + imageName)), response.getOutputStream());
        } catch (Exception e) {
            logger.error("读取图片错误" + imageName + e.getMessage());
        }
    }

    /*
    * uploadImage
    * */ //向服务器上传数据一般是用post，post有个postDate 可以将图片存在二进制的流之中
    //用get，图片的字段将不知道存在哪里
    @RequestMapping(path = {"/uploadImage/"}, method = {RequestMethod.POST})
    @ResponseBody
    public String uploadImage(@RequestParam("file") MultipartFile file) {
       // @RequestParam("file") MultipartFile file 核心的数据
        //上传的一个文件名：file，图片通过文经通过一个二进制的流的形式传进来，
        // MutipartFile是spring里面定义的接口，它封装了用户在上传图片时所包含的所有信息，
        // 但是有些时候我们要将file转换成MutipartFile，才能在保持原有代码逻辑的情况下方便代码的调整，
        // 但是file不能直接转换成MutipartFile，现在就要教大家如何将file转换成MutipartFile。
        //如果要转换，要用到的包是spring-test

        //假设存在自己的电脑上
        try {
            //String fileUrl = newsService.saveImage(file);本地路径上传
           //Qiniu云上传
            String fileUrl = qiniuService.saveImage(file);
            if (fileUrl == null) {
                return ToutiaoUtil.getJSONString(1, "上传图片失败");
            }
            return ToutiaoUtil.getJSONString(0, fileUrl);
        } catch (Exception e) {
            logger.error("上传图片失败" + e.getMessage());
            return ToutiaoUtil.getJSONString(1, "上传失败");
        }
    }
   //资讯发布
    @RequestMapping(path = {"/user/addNews/"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addNews(@RequestParam("image") String image,
                          @RequestParam("title") String title,
                          @RequestParam("link") String link) {
       //把信息入库
        try {
            News news = new News();
            if (hostHolder.getUser() != null) {
                //登陆了
                news.setUserId(hostHolder.getUser().getId());
            } else {
                // 设置一个匿名用户
                news.setUserId(3);
            }
            news.setCreatedDate(new Date());
            news.setTitle(title);
            news.setImage(image);
            news.setLink(link);
            //插入到newService
            newsService.addNews(news);
            return ToutiaoUtil.getJSONString(0);
        } catch (Exception e) {
            logger.error("添加资讯失败" + e.getMessage());
            //前台回复
            return ToutiaoUtil.getJSONString(1, "发布失败");
        }
    }
/*
* addComment加入评论
* */
    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("newsId") int newsId,
                         @RequestParam("content") String content) {
        try {
          //产生一个评论
            Comment comment = new Comment();
            comment.setUserId(hostHolder.getUser().getId());
            comment.setContent(content);
            comment.setEntityType(EntityType.ENTITY_NEWS);
            comment.setEntityId(newsId);
            comment.setCreatedDate(new Date());
            comment.setStatus(0);

            //把评论加进去
            commentService.addComment(comment);

            // 更新评论数量，以后用异步实现
            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            newsService.updateCommentCount(comment.getEntityId(), count);
          //怎样异步l化
        } catch (Exception e) {
            logger.error("提交评论错误" + e.getMessage());
        }
        //返回到新闻页面
        return "redirect:/news/" + String.valueOf(newsId);
    }
}
