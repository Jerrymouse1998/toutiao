package com.nowcoder.async;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EventProducer {
    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);

    @Autowired
    JedisAdapter jedisAdapter;

    //发送事件对象到队列中
    public boolean fireEvent(EventModel eventModel) {
        try {
            //对象序列化
            String json = JSONObject.toJSONString(eventModel);
            //生成key
            String key = RedisKeyUtil.getEventQueueKey();
            //放到队列开头
            jedisAdapter.lpush(key, json);
            return true;
        } catch (Exception e) {
            logger.error("消息入队错误:" + e.getMessage());
            return false;
        }
    }
}
