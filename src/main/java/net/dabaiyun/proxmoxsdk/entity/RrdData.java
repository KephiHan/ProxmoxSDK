package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RrdData {

    private long time;
    private int maxcpu;
    private double cpu;
    private long maxmem;
    private double mem;
    private double netin;
    private int netout;
    private long maxdisk;
    private int disk;
    private double diskread;
    private double diskwrite;

}