package net.dabaiyun.proxmoxsdk.enums;

public enum DiskDeviceType {
    IDE("ide"),
    SATA("sata"),
    SCSI("scsi"),
    VirtIO_Block("virtio");

    private String string;

    DiskDeviceType(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public static DiskDeviceType getDiskDeviceTypeByString(String name) {
        for (DiskDeviceType diskDeviceType : DiskDeviceType.values()) {
            if (diskDeviceType.getString().equals(name)) {
                return diskDeviceType;
            }
        }
        return null;
    }

}
