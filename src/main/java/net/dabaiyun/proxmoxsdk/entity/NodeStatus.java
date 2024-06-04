package net.dabaiyun.proxmoxsdk.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeStatus {

    private String pveversion;
    private Double wait;
    private Memory memory;
    private Ksm ksm;
    private Swap swap;
    private Double idle;
    private Rootfs rootfs;
    private Double cpu;
    private List<String> loadavg;
    private Cpuinfo cpuinfo;
    private String kversion;
    private Long uptime;

    @Data
    public static class Memory {
        private Long total;
        private Long used;
        private Long free;
    }

    @Data
    public static class Ksm {
        private Long shared;
    }

    @Data
    public static class Swap {
        private Long total;
        private Long used;
        private Long free;
    }

    @Data
    public static class Rootfs {
        private Long avail;
        private Long total;
        private Long used;
        private Long free;
    }

    @Data
    public static class Cpuinfo {
        private Integer cores;
        private Integer cpus;
        private String flags;
        private String model;
        private Integer sockets;
        private Integer user_hz;
        private String mhz;
        private String hvm;
    }
}