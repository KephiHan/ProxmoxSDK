package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VMConfig {

    public static final String MachineType_i440fx = "i440fx";
    public static final String MachineType_q35 = "q35";

    public static final String SCSI_HW_LSI53C895A           = "lsi";
    public static final String SCSI_HW_LSI53C810            = "lsi53c810";
    public static final String SCSI_HW_MegaSAS8708EM2       = "megasas";
    public static final String SCSI_HW_VirtIO_SCSI          = "virtio-scsi-pci";
    public static final String SCSI_HW_VirtIO_SCSI_Single   = "virtio-scsi-single";
    public static final String SCSI_HW_VMware_PVSCSI        = "pvscsi";

    public static final String OsType_Linux26               = "l26";
    public static final String OsType_Linux24               = "l24";
    public static final String OsType_WinXP                 = "wxp";
    public static final String OsType_Win2000               = "w2k";
    public static final String OsType_Win2008               = "w2k8";
    public static final String OsType_Win7                  = "win7";
    public static final String OsType_Win8                  = "win8";
    public static final String OsType_Win10                 = "win10";
    public static final String OsType_Win11                 = "win11";

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
    private Map<Integer, PciDeviceConfig> pciDeviceMap = new HashMap<>();

    @Data
    public static class NetConfig {

        public static final String DeviceType_E1000 = "e1000";
        public static final String DeviceType_VirtIO = "virtio";
        public static final String DeviceType_RTL8139 = "rtl8139";
        public static final String DeviceType_VMXNET3 = "vmxnet3";

        public static final String IpConfigTypeV4_DHCP = "dhcp";
        public static final String IpConfigTypeV4_STATIC = "static";

        public static final String IpConfigTypeV6_DHCP = "dhcp";
        public static final String IpConfigTypeV6_STATIC = "static";
        public static final String IpConfigTypeV6_SLAAC = "auto";

        private String deviceType;
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
                    .append(deviceType).append("=").append(mac)
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

        public static final String DeviceType_IDE = "ide";
        public static final String DeviceType_SATA = "sata";
        public static final String DeviceType_SCSI = "scsi";
        public static final String DeviceType_VirtIO = "virtio";
        public static final String DeviceType_Unused = "unused";

        public static final String Format_RAW = "raw";
        public static final String Format_QCOW2 = "qcow2";
        public static final String Format_VMDK = "vmdk";

        private String deviceType;
        private Integer deviceNumber;
        private String storage;
        private String folder;
        private String filename;
        private String format;
        private boolean iothread = false;
        private boolean backup = true;
        private boolean discard = false;
        private boolean replicate = true;
        private boolean readonly = false;
        private boolean ssd = false;
        private long sizeBytes = 0L;
        private boolean isCDROM = false;

        public String getDiskSizeString() {
            return diskSizeToHumanly(sizeBytes);
        }

        public String toConfigLine() {
            StringBuilder configLineBuilder = new StringBuilder();
            configLineBuilder
                    .append(storage).append(":").append(folder).append("/").append(filename);
            if (iothread)
                configLineBuilder.append(",").append("iothread=").append("1");
            if (!backup)
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
                configLineBuilder.append(",").append("size=").append(diskSizeToHumanly(sizeBytes));
            }

            return configLineBuilder.toString();
        }

        private String diskSizeToHumanly(long sizeBytes) {
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

    @Data
    public static class PciDeviceConfig {

        private String pciBus;
        private String mdev;
        private Boolean pcie;
        private Boolean rombar;

        public String toConfigLine() {
            StringBuilder configLineBuilder = new StringBuilder();
            configLineBuilder.append(pciBus);
            if (mdev != null) {
                configLineBuilder.append(",").append("mdev=").append(mdev);
            }
            if (pcie != null && pcie) {
                configLineBuilder.append(",").append("pcie=").append("1");
            }
            if (rombar != null && !rombar) {
                configLineBuilder.append(",").append("rombar=").append("0");
            }
            return configLineBuilder.toString();
        }
    }

}