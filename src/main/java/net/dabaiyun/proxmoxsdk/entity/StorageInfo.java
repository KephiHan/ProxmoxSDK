package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageInfo {

    private String storage;
    private Long total;
    private Long used;
    private Long avail;
    private String type;
    private Boolean active;
    private Boolean shared;
    @JsonProperty("used_fraction")
    private Double usedFraction;
    private String content;

}
