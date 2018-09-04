package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.*;
import com.nowcoder.util.ToutiaoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nowcoder on 2018/7/2.
   评论中心：MessageController
 *conversationDetail：RequestMethod.GET

 */
@Controller
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;
/**
 *conversationDetail
 * */
    @RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
    public String conversationDetail(Model model, @RequestParam("conversationId") String conversationId) {
        try {
            List<Message> conversationList = messageService.getConversationDetail(conversationId, 0, 10);
            List<ViewObject> messages = new ArrayList<>();
            for (Message msg : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("message", msg);
                User user = userService.getUser(msg.getFromId());
                if (user == null) {
                    continue;
                }
                //显示头像
                vo.set("headUrl", user.getHeadUrl());
                vo.set("userName", user.getName());
                messages.add(vo);
            }
           //把message加入到model里面，然后就可以到detil里面循环
            model.addAttribute("messages", messages);
            return "letterDetail";
        } catch (Exception e) {
            logger.error("获取站内信列表失败" + e.getMessage());
        }
        return "letterDetail";
    }

    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String conversationList(Model model) {
        try {
            int localUserId = hostHolder.getUser().getId();
            List<ViewObject> conversations = new ArrayList<>();
            List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
            for (Message msg : conversationList) {
                ViewObject vo = new ViewObject();
                vo.set("conversation", msg);

                //为了找出交互的那个人是我还是别人
                int targetId = msg.getFromId() == localUserId ? msg.getToId() : msg.getFromId();
                //得到这个人的详细信息
                User user = userService.getUser(targetId);
                 vo.set("user",user);
                vo.set("headUrl", user.getHeadUrl());
                vo.set("userName", user.getName());
                vo.set("targetId", targetId);
                vo.set("totalCount", msg.getId());
                //未读的消息数量
                vo.set("unreadCount", messageService.getUnreadCount(localUserId, msg.getConversationId()));
                //有了这个用户了
                conversations.add(vo);
            }
            model.addAttribute("conversations", conversations);
            return "letter";
        } catch (Exception e) {
            logger.error("获取站内信列表失败" + e.getMessage());
        }
        return "letter";
    }
/*
*addMessage
 */
    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("fromId") int fromId,
                                   @RequestParam("toId") int toId,
                                   @RequestParam("content") String content) {
        try{
            Message msg = new Message();
            msg.setContent(content);
            msg.setCreatedDate(new Date());
            msg.setToId(toId);
            msg.setFromId(fromId);
            //把fromId和toId连接在一起：那一个小哪一个放在前：三目用算符号
            msg.setConversationId(fromId < toId ? String.format("%d_%d", fromId, toId) :
                    String.format("%d_%d", toId, fromId));
           //通过service，加入一个message：
            messageService.addMessage(msg);
            return ToutiaoUtil.getJSONString(msg.getId());
        } catch (Exception e){
            logger.error("评论失败"+ e.getMessage());
            return ToutiaoUtil.getJSONString(1,"插入评论失败");
        }

    }
}


















