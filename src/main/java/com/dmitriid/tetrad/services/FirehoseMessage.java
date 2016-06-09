package com.dmitriid.tetrad.services;

public class FirehoseMessage implements Cloneable {
  public String type;  // "slack", "xmpp"
  public String subtype; // "post", "edit", "subscribe", "login"
  public String user; // "xyz", "foo@bar
  public String service; // "c.j.r", "erlyclub"
  public String channel; // "random", "erlang-talks", "erlanger"
  public String content;

  public FirehoseMessage(){} // for Jackson
  public FirehoseMessage(
      String type,
      String subtype,
      String user,
      String service,
      String channel,
      String content
  ){
    this.type = type;
    this.subtype = subtype;
    this.user = user;
    this.service = service;
    this.channel = channel;
    this.content = content;
  }

  @Override
  public Object clone(){
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      return new FirehoseMessage(
              this.type,
              this.subtype,
              this.user,
              this.service,
              this.channel,
              this.content
      );
    }
  }

}
