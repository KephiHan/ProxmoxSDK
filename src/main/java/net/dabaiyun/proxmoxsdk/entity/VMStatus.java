package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VMStatus {

    public static final String Statu_Running = "running";
    public static final String Statu_Stopped = "stopped";

    private int vmid;
    private int cpus;
    private double cpu;//CPU利用率
    private long diskread;
    private long netout;
    private long uptime;
    private long mem;//内存使用量
    private long maxdisk;
    private String name;
    private long maxmem;
    private long diskwrite;
    private long netin;
    private String status;

}