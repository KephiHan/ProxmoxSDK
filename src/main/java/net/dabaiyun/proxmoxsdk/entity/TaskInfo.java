package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskInfo {

    //获取Task列表时 Source可选 archive,active,all
    public static final String Source_Archive   = "archive";
    public static final String Source_Active    = "active";
    public static final String Source_All       = "all";


    public static final String Type_QmStart     = "qmstart";
    public static final String Type_QmShutdown  = "qmshutdown";
    public static final String Type_QmStop      = "qmstop";
    public static final String Type_QmReset     = "qmreset";
    public static final String Type_QmReboot    = "qmreboot";
    public static final String Type_QmPause     = "qmpause";
    public static final String Type_QmResume    = "qmresume";
    public static final String Type_QmSuspend   = "qmsuspend";
    public static final String Type_QmClone     = "qmclone";
    public static final String Type_QmDestroy   = "qmdestroy";
    public static final String Type_VzDump      = "vzdump";
    public static final String Type_ImgCopy     = "imgcopy";
    public static final String Type_ImgDel      = "imgdel";
    public static final String Type_QmMove      = "qmmove";


    public static final String ExitStatus_OK    = "OK";

    public static final String Status_Stopped   = "stopped";
    public static final String Status_Running   = "running";

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
