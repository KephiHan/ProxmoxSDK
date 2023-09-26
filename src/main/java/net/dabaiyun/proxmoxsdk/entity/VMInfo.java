package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VMInfo {

    private int cpus;
    private int vmid;
    private long pid;
    private String status;
    private long uptime;
    private String name;
    private double cpu;
    private long balloon_min;
    private int shares;
    private long mem;
    private long maxmem;
    private int disk;
    private long maxdisk;
    private int diskwrite;
    private int diskread;
    private long netin;
    private long netout;

}