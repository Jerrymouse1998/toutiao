package com.nowcoder.async;

import com.alibaba.fastjson.JSON;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    //每种事件类型对应的处理器列表
    private Map<EventType, List<EventHandler>> config = new HashMap<>();
    //应用上下文
    private ApplicationContext applicationContext;
    @Autowired
    private JedisAdapter jedisAdapter;

    //初始化
    @Override
    public void afterPropertiesSet() throws Exception {
        //从应用上下文中获取所有EventHandler类型的bean
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null) {
            //遍历容器中所有的事件处理器
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                //拿到每个事件处理器所关注的事件类型列表
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();
                //遍历事件类型列表
                for (EventType type : eventTypes) {
                    //生成(key:事件类型,value:处理器1..处理器2..处理器n)映射表
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<EventHandler>());
                    }
                    config.get(type).add(entry.getValue());
                }
            }
        }

        // 启动线程去消费事件
        Thread thread = new Thread(() -> {
            // 从队列一直消费
            while (true) {
                String key = RedisKeyUtil.getEventQueueKey();
                //从队列中拿到事件列表，0表示当队列为空一直保持阻塞
                List<String> messages = jedisAdapter.brpop(0, key);
                //遍历取出的事件列表
                for (String message : messages) {
                    // 第一个元素是队列名字，跳过
                    if (message.equals(key)) {
                        continue;
                    }
                    //反序列化
                    EventModel eventModel = JSON.parseObject(message, EventModel.class);
                    //检查这个事件类型是否有对应的处理器
                    if (!config.containsKey(eventModel.getType())) {
                        logger.error("不能识别的事件");
                        continue;
                    }
                    //将事件对象路由给对应的事件处理器进行处理
                    for (EventHandler handler : config.get(eventModel.getType())) {
                        handler.doHandle(eventModel);
                    }
                }
            }
        });
        thread.start();
    }

    //拿到应用上下文
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
