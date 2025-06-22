package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PciDevice {

    @JsonProperty("device")
    private String deviceId;
    @JsonProperty("device_name")
    private String deviceName;
    @JsonProperty("id")
    private String pciBus;
    @JsonProperty("vendor")
    private String vendorId;
    @JsonProperty("vendor_name")
    private String vendorName;
    @JsonProperty("class")
    private String _class;
    @JsonProperty("subsystem_vendor")
    private String subsystemVendor;
    @JsonProperty("subsystem_device")
    private String subsystemDevice;
    @JsonProperty("iommugroup")
    private Integer iommuGroup;
    @JsonProperty("mdev")
    private Integer mdev;

}
