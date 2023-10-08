package net.dabaiyun.proxmoxsdk.entity;

import lombok.Data;

@Data
public class IpCidr {

    private String digest;
    private String cidr;
    private String comment;
}
