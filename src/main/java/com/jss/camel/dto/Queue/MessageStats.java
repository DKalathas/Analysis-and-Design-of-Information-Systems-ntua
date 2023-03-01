package com.jss.camel.dto.Queue;

public class MessageStats{
    public int ack;
    public AckDetails ack_details;
    public int deliver;
    public DeliverDetails deliver_details;
    public int deliver_get;
    public DeliverGetDetails deliver_get_details;
    public int deliver_no_ack;
    public DeliverNoAckDetails deliver_no_ack_details;
    public int get;
    public GetDetails get_details;
    public int get_empty;
    public GetEmptyDetails get_empty_details;
    public int get_no_ack;
    public GetNoAckDetails get_no_ack_details;
    public int publish;
    public PublishDetails publish_details;
    public int redeliver;
    public RedeliverDetails redeliver_details;
}