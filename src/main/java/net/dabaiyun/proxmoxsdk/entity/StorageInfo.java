package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.dabaiyun.proxmoxsdk.enums.StorageContent;
import net.dabaiyun.proxmoxsdk.enums.StorageType;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageInfo {

    private String storage;
    private Long total;
    private Long used;
    private Long avail;
    private String type;
    private Boolean active;
    private Integer shared;
    @JsonProperty("used_fraction")
    private Double usedFraction;
    private String content;

    @JsonIgnore
    private StorageType storageType;
    @JsonIgnore
    private List<StorageContent> contentList;

}
