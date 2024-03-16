package net.dabaiyun.proxmoxsdk.enums;

public enum StorageType {
    DIR("dir"),
    NFS("nfs")
    ;

    private String string;

    StorageType(String string){
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public static StorageType getStorageTypeByString(String string) {
        for (StorageType storageType : StorageType.values()) {
            if (storageType.getString().equals(string)) {
                return storageType;
            }
        }
        return null;
    }
}
