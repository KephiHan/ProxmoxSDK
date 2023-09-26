package net.dabaiyun.proxmoxsdk.entity;
import lombok.Data;

import java.util.List;

@Data
public class NodeStatus {

    private String pveversion;
    private int wait;
    private Memory memory;
    private Ksm ksm;
    private Swap swap;
    private int idle;
    private Rootfs rootfs;
    private int cpu;
    private List<String> loadavg;
    private Cpuinfo cpuinfo;
    private String kversion;
    private long uptime;

    @Data
    public class Memory {
        private long total;
        private long used;
        private long free;
    }

    @Data
    public class Ksm {
        private long shared;
    }

    @Data
    public class Swap {
        private long total;
        private long used;
        private long free;
    }

    @Data
    public class Rootfs {
        private long avail;
        private long total;
        private long used;
        private long free;
    }

    @Data
    public class Cpuinfo {
        private int cores;
        private int cpus;
        private String flags;
        private String model;
        private int sockets;
        private int user_hz;
        private String mhz;
        private String hvm;
    }
}