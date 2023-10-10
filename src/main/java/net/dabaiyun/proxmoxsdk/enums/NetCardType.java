package net.dabaiyun.proxmoxsdk.enums;

public enum NetCardType {
    E1000("e1000"),
    VirtIo("virtio"),
    RTL8139("rtl8139"),
    VmxNet3("vmxnet3");

    private String string;

    NetCardType(String string){
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public String toString(){
        return string;
    }
}
