package com.nowcoder.service;

import com.nowcoder.dao.NewsDAO;
import com.nowcoder.model.News;
import com.nowcoder.util.ToutiaoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;


@Service
public class NewsService {
    @Autowired
    private NewsDAO newsDAO;

    //分页查询，有userId根据id查，没有则查所有
    public List<News> getLatestNews(int userId, int offset, int limit) {
        return newsDAO.selectByUserIdAndOffset(userId, offset, limit);
    }

    //添加一条资讯
    public int addNews(News news) {
        newsDAO.addNews(news);
        return news.getId();
    }

    //根据资讯id找资讯
    public News getById(int newsId) {
        return newsDAO.getById(newsId);
    }

    //文件上传到服务器本地
    public String saveImage(MultipartFile file) throws IOException {
        //拿到.的索引
        int dotPos = file.getOriginalFilename().lastIndexOf(".");
        //文件名中没有.，不合法的文件名直接返回null
        if (dotPos < 0) {
            return null;
        }
        //判断一下后缀名是否合法
        String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase();
        if (!ToutiaoUtil.isFileAllowed(fileExt)) {
            return null;
        }
        //重写文件名
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + "." + fileExt;
        //文件copy到本地，文件名重复就进行覆盖
        Files.copy(file.getInputStream(), new File(ToutiaoUtil.IMAGE_DIR + fileName).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        //文件成功保存后，返回路径(给前端使用)
        return ToutiaoUtil.TOUTIAO_DOMAIN + "image?name=" + fileName;
    }

    //更新评论数
    public int updateCommentCount(int id, int count) {
        return newsDAO.updateCommentCount(id, count);
    }
}
