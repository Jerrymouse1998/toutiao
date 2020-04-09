package com.nowcoder.model;

import org.springframework.stereotype.Component;


@Component//HostHolder以注解的方式生成bean，方便后续service等方法中去使用。
public class HostHolder {
    //因为系统不是一个人访问的，所以将user信息存放到请求的线程本地变量中
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();;
    }
}
