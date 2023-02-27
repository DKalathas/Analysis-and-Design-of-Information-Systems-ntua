package com.jss.camel.dto.Conn;

public class Root{
    public int acks_uncommitted;
    public boolean confirm;
    public ConnectionDetails connection_details;
    public int consumer_count;
    public GarbageCollection garbage_collection;
    public int global_prefetch_count;
    //private String idle_since;
    public MessageStats message_stats;
    public int messages_unacknowledged;
    public int messages_uncommitted;
    public int messages_unconfirmed;
    public String name;
    public String node;
    public int number;
    public int pending_raft_commands;
    public int prefetch_count;
    public int reductions;
    public ReductionsDetails reductions_details;
    public String state;
    public boolean transactional;
    public String user;
    public String user_who_performed_action;
    public String vhost;
}

