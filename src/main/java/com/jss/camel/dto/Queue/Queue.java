package com.jss.camel.dto.Queue;

public class Queue {
    public Arguments arguments;
    public boolean auto_delete;
    public BackingQueueStatus backing_queue_status;
    public int consumer_capacity;
    public int consumer_utilisation;
    public int consumers;
    public boolean durable;
    public EffectivePolicyDefinition effective_policy_definition;
    public boolean exclusive;
    public Object exclusive_consumer_tag;
    public GarbageCollection garbage_collection;
    public Object head_message_timestamp;
    public String idle_since;
    public int memory;
    public int message_bytes;
    public int message_bytes_paged_out;
    public int message_bytes_persistent;
    public int message_bytes_ram;
    public int message_bytes_ready;
    public int message_bytes_unacknowledged;
    public MessageStats message_stats;
    public int messages;
    public MessagesDetails messages_details;
    public int messages_paged_out;
    public int messages_persistent;
    public int messages_ram;
    public int messages_ready;
    public MessagesReadyDetails messages_ready_details;
    public int messages_ready_ram;
    public int messages_unacknowledged;
    public MessagesUnacknowledgedDetails messages_unacknowledged_details;
    public int messages_unacknowledged_ram;
    public String name;
    public String node;
    public Object operator_policy;
    public Object policy;
    public Object recoverable_slaves;
    public int reductions;
    public ReductionsDetails reductions_details;
    public Object single_active_consumer_tag;
    public String state;
    public String type;
    public String vhost;
}

