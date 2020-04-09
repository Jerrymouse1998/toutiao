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
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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

    @RequestMapping(path = {"/news/{newsId}"}, method = {RequestMethod.GET})
    public String newsDetail(@PathVariable("newsId") int newsId, Model model) {
        try {
            News news = newsService.getById(newsId);
            if (news != null) {
                List<Comment> comments = commentService.getCommentsByEntity(news.getId(), EntityType.ENTITY_NEWS);
                List<ViewObject> commentVOs = new ArrayList<ViewObject>();
                for (Comment comment : comments) {
                    ViewObject commentVO = new ViewObject();
                    commentVO.set("comment", comment);
                    commentVO.set("user", userService.getUser(comment.getUserId()));
                    commentVOs.add(commentVO);
                }
                model.addAttribute("comments", commentVOs);
            }
            model.addAttribute("news", news);
            model.addAttribute("owner", userService.getUser(news.getUserId()));
        } catch (Exception e) {
            logger.error("获取资讯明细错误" + e.getMessage());
        }
        return "detail";
    }

    //获取(下载)图片
    @RequestMapping(path = {"/image"}, method = {RequestMethod.GET})
    @ResponseBody
    public void getImage(@RequestParam("name") String imageName,
                         HttpServletResponse response) {
        try {
            //设置响应头
            response.setContentType("image/jpeg");
            //将本地的目标文件作为输入流，copy到response的输出流中
            StreamUtils.copy(new FileInputStream(new
                    File(ToutiaoUtil.IMAGE_DIR + imageName)), response.getOutputStream());
        } catch (Exception e) {
            logger.error("读取图片错误" + imageName + e.getMessage());
        }
    }

    //上传图片
    @RequestMapping(path = {"/uploadImage/"}, method = {RequestMethod.POST})
    @ResponseBody
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            //String fileUrl = newsService.saveImage(file);
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

    //发布资讯
    @RequestMapping(path = {"/user/addNews/"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addNews(@RequestParam("image") String image,
                          @RequestParam("title") String title,
                          @RequestParam("link") String link) {
        try {
            News news = new News();
            news.setCreatedDate(new Date());
            news.setTitle(title);
            news.setImage(image);
            news.setLink(link);
            //到HostHolder中拿用户信息
            if (hostHolder.getUser() != null) {//用户登录
                news.setUserId(hostHolder.getUser().getId());
            } else {//未登录
                // 设置一个匿名用户，假userId：3是一个定义好的匿名用户
                news.setUserId(3);
            }
            newsService.addNews(news);
            return ToutiaoUtil.getJSONString(0);
        } catch (Exception e) {
            logger.error("添加资讯失败" + e.getMessage());
            return ToutiaoUtil.getJSONString(1, "发布失败");
        }
    }

    //发布评论
    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("newsId") int newsId,
                             @RequestParam("content") String content) {
        try {
            Comment comment = new Comment();
            comment.setUserId(hostHolder.getUser().getId());
            comment.setContent(content);
            comment.setEntityType(EntityType.ENTITY_NEWS);
            comment.setEntityId(newsId);
            comment.setCreatedDate(new Date());
            comment.setStatus(0);
            commentService.addComment(comment);

            // 更新评论数量，以后用异步实现
            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            newsService.updateCommentCount(comment.getEntityId(), count);

        } catch (Exception e) {
            logger.error("提交评论错误" + e.getMessage());
        }
        return "redirect:/news/" + String.valueOf(newsId);
    }

}
