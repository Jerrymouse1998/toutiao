package com.nowcoder.async;

import java.util.HashMap;
import java.util.Map;


public class EventModel {
    //事件类型
    private EventType type;
    //事件发起者
    private int actorId;
    //触发对象
    private int entityId;
    private int entityType;
    //触发对象的拥有者
    private int entityOwnerId;
    //事件触发现场参数，扩展信息extend
    private Map<String, String> exts = new HashMap<>();

    public Map<String, String> getExts() {
        return exts;
    }

    public EventModel() {

    }

    public EventModel(EventType type) {
        this.type = type;
    }

    public String getExt(String name) {
        return exts.get(name);
    }

    public EventModel setExt(String name, String value) {
        exts.put(name, value);
        return this;
    }

    public EventType getType() {
        return type;
    }

    public EventModel setType(EventType type) {
        this.type = type;
        return this;
    }

    public int getActorId() {
        return actorId;
    }

    public EventModel setActorId(int actorId) {
        this.actorId = actorId;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public EventModel setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public EventModel setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityOwnerId() {
        return entityOwnerId;
    }

    public EventModel setEntityOwnerId(int entityOwnerId) {
        this.entityOwnerId = entityOwnerId;
        return this;
    }
}
