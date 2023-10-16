package net.dabaiyun.proxmoxsdk.enums;

public enum NetDeviceType {
    E1000("e1000"),
    VirtIo("virtio"),
    RTL8139("rtl8139"),
    VmxNet3("vmxnet3");

    private String string;

    NetDeviceType(String string){
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public String toString(){
        return string;
    }

    public static NetDeviceType getNetCardTypeByString(String name){
        for (NetDeviceType netDeviceType : NetDeviceType.values()) {
            if (netDeviceType.getString().equals(name)) {
                return netDeviceType;
            }
        }
        return null;
    }
}
