package net.dabaiyun.proxmoxsdk.enums;

public enum StorageContent {
    Images("images"),
    VzTmpl("vztmpl"),
    BackUp("backup"),
    Snippets("snippets"),
    RootDir("rootdir"),
    Iso("iso")
    ;

    private String string;

    StorageContent(String string){
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public static StorageContent getStorageContentByString(String string) {
        for (StorageContent storageContent : StorageContent.values()) {
            if (storageContent.getString().equals(string)) {
                return storageContent;
            }
        }
        return null;
    }
}
