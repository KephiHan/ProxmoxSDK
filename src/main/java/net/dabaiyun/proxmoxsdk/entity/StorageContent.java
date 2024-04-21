package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageContent {

    public static final String ContentType_ISO      = "iso";
    public static final String ContentType_IMAGE    = "images";
    public static final String ContentType_BACKUP   = "backup";

    private String volid;  //eg. "local:iso/cn_windows_server_2019_x64_dvd_4de40f33.iso"
    private String format;
    private Long size;
    private Long used;
    private Long ctime;
    private String content;
    private Integer vmid;

    //附加信息
    private String storage;
    private String folder;
    private String filename;

    public void setVolid(String volid){
        this.volid = volid;
        //解析volid字符串
        String[] strlist1 = volid.split("/");
        String storageAndFolder = strlist1[0];
        if(strlist1.length >= 2){
            this.filename = strlist1[1];
        }else {
            this.filename = "";
        }
        String[] strlist2 = storageAndFolder.split(":");
        this.storage = strlist2[0];
        if(strlist2.length >= 2){
            this.folder = strlist2[1];
        }else{
            this.folder = "";
        }
    }
}
