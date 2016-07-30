package com.afollestad.nocknock.api;

import com.afollestad.inquiry.annotations.Column;

import java.io.Serializable;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ServerModel implements Serializable {

    public ServerModel() {
    }

    @Column(name = "_id", primaryKey = true, notNull = true, autoIncrement = true)
    public long id;
    @Column
    public String name;
    @Column
    public String url;
    @Column
    public int status;
    @Column
    public long checkInterval;
    @Column
    public long lastCheck;
}