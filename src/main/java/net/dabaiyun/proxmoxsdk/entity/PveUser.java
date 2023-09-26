package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PveUser {

    @JsonProperty("realm-type")
    private String realmType;
    private String userid;
    private int enable;
    private int expire;
    private List<String> groups;
    private String tokens;
    private String comment;
    private String email;
}
