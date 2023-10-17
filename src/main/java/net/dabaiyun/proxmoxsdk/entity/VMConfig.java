package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.dabaiyun.proxmoxsdk.enums.DiskDeviceType;
import net.dabaiyun.proxmoxsdk.enums.NetDeviceType;

import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VMConfig {

    private String name;

    private String cpu;
    private int sockets;
    private int numa;
    private int cores;

    private int memory;
    private int balloon;
    private int shares;

    private String ostype;
    private String vmgenid;
    private String smbios1;
    private String scsihw;
    private String machine;
    private String digest;

    private String boot;

    private List<String> bootOrder = new ArrayList<>();
    private Map<Integer, NetConfig> netDeviceMap = new HashMap<>();
    private Map<String, DiskConfig> diskConfigMap = new HashMap<>();

    @Data
    public static class NetConfig {
        private NetDeviceType netDeviceType;
        private String mac;
        private String bridge;
        private boolean firewall = false;
        private boolean linkDown = false;
        private int mtu = 0;
        private int multiQueue = 0;
        private int limitRateMBps = 0;
        private int vlanId = 0;

        public String toConfigLine() {
            StringBuilder configLineBuilder = new StringBuilder();
            configLineBuilder
                    .append(netDeviceType.getString()).append("=").append(mac)
                    .append(",")
                    .append("bridge=").append(bridge);
            if (firewall)
                configLineBuilder.append(",").append("firewall=").append("1");
            if (linkDown)
                configLineBuilder.append(",").append("link_down=").append("1");
            if (mtu != 0)
                configLineBuilder.append(",").append("mtu=").append(mtu);
            if (multiQueue != 0)
                configLineBuilder.append(",").append("queues=").append(multiQueue);
            if (limitRateMBps != 0)
                configLineBuilder.append(",").append("rate=").append(limitRateMBps);
            if (vlanId != 0)
                configLineBuilder.append(",").append("tag=").append(vlanId);
            return configLineBuilder.toString();
        }
    }

    @Data
    public static class DiskConfig {
        private DiskDeviceType diskDeviceType;
        private String storage;
        private String folder;
        private String filename;
        private boolean iothread = false;
        private boolean backup = true;
        private boolean discard = false;
        private boolean replicate = true;
        private boolean readonly = false;
        private boolean ssd = false;
        private long sizeBytes = 0L;
        private boolean isCDROM = false;

        public String getDiskSizeString(){
            return diskSizeToHumanly(sizeBytes);
        }

        public String toConfigLine() {
            StringBuilder configLineBuilder = new StringBuilder();
            configLineBuilder
                    .append(storage).append(":").append(folder).append("/").append(filename);
            if (iothread)
                configLineBuilder.append(",").append("iothread=").append("1");
            if (backup)
                configLineBuilder.append(",").append("backup=").append("0");
            if (discard)
                configLineBuilder.append(",").append("discard=").append("on");
            if (replicate)
                configLineBuilder.append(",").append("replicate=").append("0");
            if (readonly)
                configLineBuilder.append(",").append("ro=").append("1");
            if (ssd)
                configLineBuilder.append(",").append("ssd=").append("1");
            if (isCDROM)
                configLineBuilder.append(",").append("media=").append("cdrom");
            if (sizeBytes != 0) {
                configLineBuilder.append(",").append("size=")
                        .append(diskSizeToHumanly(sizeBytes));
            }

            return configLineBuilder.toString();
        }

        private String diskSizeToHumanly(long sizeBytes){
            String sizeString = "";
            if (sizeBytes % Math.pow(1024, 4) == 0) {
                sizeString = (int) (sizeBytes * Math.pow(1024, -4)) + "T";
            } else if (sizeBytes % Math.pow(1024, 3) == 0) {
                sizeString = (int) (sizeBytes * Math.pow(1024, -3)) + "G";
            } else if (sizeBytes % Math.pow(1024, 2) == 0) {
                sizeString = (int) (sizeBytes * Math.pow(1024, -2)) + "M";
            } else if (sizeBytes % Math.pow(1024, 1) == 0) {
                sizeString = (int) (sizeBytes * Math.pow(1024, -1)) + "K";
            } else {
                sizeString = (int) sizeBytes + "B";
            }
            return sizeString;
        }
    }


    /**
     * 根据boot字符串解析第一引导磁盘
     *
     * @return 磁盘位置，例如virtio0 scsi0 ide1
     */
    public String getFirstBootDisk() {
        /*
            boot样例   order=virtio0;ide2
         */
        if (this.boot == null) {
            return null;
        }
        String[] bootDrivers = this.boot.split(";");
        //如果有数据，则解析等号=
        if (bootDrivers.length >= 1) {
            String[] orderAndDriver = bootDrivers[0].split("=");
            //如果只分割出了一个字符串，直接返回
            if (orderAndDriver.length == 1) {
                return orderAndDriver[0];
            } else if (orderAndDriver.length == 2) {
                //如果分割出两个，则返回第二个字符串
                return orderAndDriver[1];
            } else {
                //出错情况，返回null
                return null;
            }
        }
        //出错情况，返回null
        return null;
    }

}