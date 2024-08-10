package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageContent {

    public static final String ContentType_ISO      = "iso";
    public static final String ContentType_IMAGE    = "images";
    public static final String ContentType_BACKUP   = "backup";

    private String volid;
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

    //备份类型专有信息
    private String notes;
    @JsonProperty("protected")
    private Boolean protect;
    private String subtype;

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
        if(strlist2.length >= 2){
            this.storage = strlist2[0];
            this.folder = strlist2[1];
        }else{
            this.storage = "";
            this.folder = "";
        }
    }

    /*  IMAGE     "HDD-Raid6:1001/vm-1001-disk-0.qcow2"
        {
            "volid" : "HDD-Raid6:1001/vm-1001-cloudinit.qcow2",
            "format" : "qcow2",
            "size" : 4194304,
            "used" : 638976,
            "ctime" : 1722843193,
            "content" : "images",
            "vmid" : 1001,
            "storage" : "HDD-Raid6",
            "folder" : "1001",
            "filename" : "vm-1001-cloudinit.qcow2"
        }
     */

    /*  ISO       "local:iso/cn_windows_server_2019_x64_dvd_4de40f33.iso"
        {
            "volid" : "HDD-Raid6:iso/CentOS-7-x86_64-DVD-2003.iso",
            "format" : "iso",
            "size" : 4781506560,
            "used" : null,
            "ctime" : 1712924439,
            "content" : "iso",
            "vmid" : null,
            "storage" : "HDD-Raid6",
            "folder" : "iso",
            "filename" : "CentOS-7-x86_64-DVD-2003.iso"
        }
     */

    /*  BACKUP    "HDD-Raid6:backup/vzdump-qemu-1001-2024_08_03-05_05_23.vma.zst"
        {
            "volid" : "HDD-Raid6:backup/vzdump-qemu-1001-2024_08_03-05_05_23.vma.zst",
            "format" : "vma.zst",
            "size" : 9874594606,
            "used" : null,
            "ctime" : 1722632723,
            "content" : "backup",
            "vmid" : 1001,
            "storage" : "HDD-Raid6",
            "folder" : "backup",
            "filename" : "vzdump-qemu-1001-2024_08_03-05_05_23.vma.zst"
        }
     */




}
