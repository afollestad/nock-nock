package com.afollestad.nocknock.api;

import static com.afollestad.nocknock.ui.MainActivity.SITES_TABLE_NAME;

import com.afollestad.inquiry.annotations.Column;
import com.afollestad.inquiry.annotations.Table;
import java.io.Serializable;

/** @author Aidan Follestad (afollestad) */
@Table(name = SITES_TABLE_NAME)
public class ServerModel implements Serializable {

  public ServerModel() {}

  @Column(name = "_id", primaryKey = true, notNull = true, autoIncrement = true)
  public long id;

  @Column public String name;
  @Column public String url;
  @Column @ServerStatus.Enum public int status;
  @Column public long checkInterval;
  @Column public long lastCheck;
  @Column public String reason;

  @Column @ValidationMode.Enum public int validationMode;
  @Column public String validationContent;
}
