package com.nowcoder.async;

import java.util.List;

//事件处理器接口
public interface EventHandler {
    //处理消息的方法
    void doHandle(EventModel model);
    //处理器所关注的事件类型
    List<EventType> getSupportEventTypes();
}
