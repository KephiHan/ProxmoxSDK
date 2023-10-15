package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.dabaiyun.proxmoxsdk.enums.NetCardType;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VMConfig {

    private int numa;
    private int sockets;
    private int cores;
    private String cpu;
    private int shares;
    private int balloon;
    private int memory;
    private String ostype;
    private String vmgenid;
    private String smbios1;
    private String scsihw;
    private String machine;
    private String digest;
    private String name;
    private String boot;
    private String efidisk0;
    private String tpmstate0;

    private String ide0;
    private String ide1;
    private String ide2;
    private String ide3;
    private String scsi0;
    private String scsi1;
    private String scsi2;
    private String scsi3;
    private String scsi4;
    private String scsi5;
    private String scsi6;
    private String scsi7;
    private String scsi8;
    private String scsi9;
    private String scsi10;
    private String scsi11;
    private String scsi12;
    private String scsi13;
    private String scsi14;
    private String scsi15;
    private String scsi16;
    private String scsi17;
    private String scsi18;
    private String scsi19;
    private String scsi20;
    private String scsi21;
    private String scsi22;
    private String scsi23;
    private String scsi24;
    private String scsi25;
    private String scsi26;
    private String scsi27;
    private String scsi28;
    private String scsi29;
    private String scsi30;
    private String virtio0;
    private String virtio1;
    private String virtio2;
    private String virtio3;
    private String virtio4;
    private String virtio5;
    private String virtio6;
    private String virtio7;
    private String virtio8;
    private String virtio9;
    private String virtio10;
    private String virtio11;
    private String virtio12;
    private String virtio13;
    private String virtio14;
    private String virtio15;
    private String sata0;
    private String sata1;
    private String sata2;
    private String sata3;
    private String sata4;
    private String sata5;

    private String net0;
    private String net1;
    private String net2;
    private String net3;
    private String net4;
    private String net5;
    private String net6;
    private String net7;

    /**
     * 根据boot字符串解析第一引导磁盘
     * @return 磁盘位置，例如virtio0 scsi0 ide1
     */
    public String getFirstBootDisk(){
        /*
            boot样例   order=virtio0;ide2
         */
        if(this.boot == null){
            return null;
        }
        String[] bootDrivers = this.boot.split(";");
        //如果有数据，则解析等号=
        if(bootDrivers.length >= 1){
            String[] orderAndDriver = bootDrivers[0].split("=");
            //如果只分割出了一个字符串，直接返回
            if(orderAndDriver.length == 1){
                return orderAndDriver[0];
            }else if(orderAndDriver.length == 2){
                //如果分割出两个，则返回第二个字符串
                return orderAndDriver[1];
            }else{
                //出错情况，返回null
                return null;
            }
        }
        //出错情况，返回null
        return null;
    }

}