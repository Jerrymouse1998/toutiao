package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Message;
import com.nowcoder.service.MessageService;
import com.nowcoder.util.MailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LoginExceptionHandler implements EventHandler{
    @Autowired
    MessageService messageService;

    @Autowired
    MailSender mailSender;

    @Override
    public void doHandle(EventModel model) {
        /*
        * 假设这里有一些判断登陆异常的操作
        * */
        //存在异常，发送站内信
        Message message = new Message();
        message.setToId(model.getActorId());
        message.setContent("你上次的登陆IP异常");
        message.setFromId(3);//假设3 是管理员的id
        message.setCreatedDate(new Date());
        messageService.addMessage(message);
        //发送邮件
        Map<String, Object> map = new HashMap();
        map.put("username", model.getExt("username"));
        mailSender.sendWithHTMLTemplate(model.getExt("email"), "toutiao项目登陆异常",
                "mails/welcome.html", map);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.LOGIN);
    }
}
