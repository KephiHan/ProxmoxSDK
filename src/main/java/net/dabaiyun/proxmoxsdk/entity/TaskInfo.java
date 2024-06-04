package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskInfo {
    public static final String Type_VzDump      = "vzdump";
    public static final String Type_ImgCopy     = "imgcopy";
    public static final String Type_ImgDel      = "imgdel";
    public static final String Type_QmStart     = "qmstart";
    public static final String Type_QmShutdown  = "qmshutdown";
    public static final String Type_QmMove      = "qmmove";

    public static final String Status_Stopped   = "stopped";
    public static final String Status_Running   = "running";
    public static final String Status_OK        = "OK";

    private String upid;
    private String node;
    private String exitstatus;
    private Integer pid;
    private Long starttime;
    private Long endtime;
    private Long pstart;
    private String id;
    private String type;
    private String user;
    private String status;

}
