package com.jss.camel.dto.Queue;

import java.util.ArrayList;

public class BackingQueueStatus {
    public double avg_ack_egress_rate;
    public double avg_ack_ingress_rate;
    public double avg_egress_rate;
    public double avg_ingress_rate;
    public ArrayList<Object> delta;
    public int len;
    public String mode;
    public int next_seq_id;
    public int q1;
    public int q2;
    public int q3;
    public int q4;
    public String target_ram_count;
}
