package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Network {

    public static final String NetworkType_Bridge = "bridge";
    public static final String NetworkType_Bond = "bond";
    public static final String NetworkType_Eth = "eth";
    public static final String NetworkType_Alias = "alias";
    public static final String NetworkType_Vlan = "vlan";
    public static final String NetworkType_Fabric = "fabric";
    public static final String NetworkType_OVSBridge = "OVSBridge";
    public static final String NetworkType_OVSBond = "OVSBond";
    public static final String NetworkType_OVSPort = "OVSPort";
    public static final String NetworkType_OVSIntPort = "OVSIntPort";
    public static final String NetworkType_Vnet = "vnet";
    public static final String NetworkType_Any_bridge = "any_bridge";
    public static final String NetworkType_Any_local_bridge = "any_local_bridge";
    public static final String NetworkType_Include_sdn = "include_sdn";

    private Integer exists;
    private Integer active;
    private Integer priority;

    private String iface;
    private String type;
    private Integer autostart;
    private Integer mtu;

    private String method;
    private String cidr;
    private String address;
    private Integer netmask;
    private String gateway;
    private String comments;

    private String method6;
    private String cidr6;
    private String address6;
    private Integer netmask6;
    private String comments6;
    private String gateway6;

    private String bond_primary;
    private String bond_mode;
    private String bond_xmit_hash_policy;

    private String bridge_ports;
    private Boolean bridge_vlan_aware;
    private String bridge_fd;
    private String bridge_stp;

    private String ovs_bonds;
    private String ovs_bridge;
    private String ovs_options;
    private String ovs_ports;
    private Integer ovs_tag;

    private String slaves;

    private Integer vlan_id;
    private String vlan_raw_device;

    private List<String> families;

//    public enum NetworkType {
//        BRIDGE("bridge"),
//        BOND("bond"),
//        ETH("eth"),
//        ALIAS("alias"),
//        VLAN("vlan"),
//        FABRIC("fabric"),
//        OVS_BRIDGE("OVSBridge"),
//        OVS_BOND("OVSBond"),
//        OVS_PORT("OVSPort"),
//        OVS_INT_PORT("OVSIntPort"),
//        VNET("vnet"),
//        ANY_BRIDGE("any_bridge"),
//        ANY_LOCAL_BRIDGE("any_local_bridge"),
//        INCLUDE_SDN("include_sdn"),
//        UNKNOWN("unknown"); // 用于处理未知值
//
//        private final String value;
//
//        NetworkType(String value) {
//            this.value = value;
//        }
//
//        @JsonValue // 序列化时使用此值
//        public String getValue() {
//            return value;
//        }
//
//        @JsonCreator // 反序列化时使用此方法
//        public static NetworkType fromValue(String value) {
//            if (value == null) {
//                return UNKNOWN;
//            }
//
//            for (NetworkType type : values()) {
//                if (type.value.equalsIgnoreCase(value)) {
//                    return type;
//                }
//            }
//
//            // 处理大小写不匹配的情况
//            return switch (value.toLowerCase()) {
//                case "bridge" -> BRIDGE;
//                case "bond" -> BOND;
//                case "eth" -> ETH;
//                case "alias" -> ALIAS;
//                case "vlan" -> VLAN;
//                case "fabric" -> FABRIC;
//                case "ovsbridge" -> OVS_BRIDGE;
//                case "ovsbond" -> OVS_BOND;
//                case "ovsport" -> OVS_PORT;
//                case "ovsintport" -> OVS_INT_PORT;
//                case "vnet" -> VNET;
//                case "any_bridge" -> ANY_BRIDGE;
//                case "any_local_bridge" -> ANY_LOCAL_BRIDGE;
//                case "include_sdn" -> INCLUDE_SDN;
//                default -> UNKNOWN;
//            };
//        }
//
//        @Override
//        public String toString() {
//            return value;
//        }
//    }


}
