import com.fasterxml.jackson.databind.ObjectMapper;
import it.corsinvest.proxmoxve.api.PveClient;
import it.corsinvest.proxmoxve.api.PveResult;
import net.dabaiyun.proxmoxsdk.ProxmoxClient;
import net.dabaiyun.proxmoxsdk.entity.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JunitTest {

    private final ProxmoxClient proxmoxClient
//            = null;
            = new ProxmoxClient(
            "js-dx-1.dabaiyun.net",
            8706,
            "root",
            "BayMax@201028"
//            = new ProxmoxClient(
//            "192.168.77.8",
//            8006,
//            "root",
//            "BayMax@201028"
    );

    private final String nodename = "pve-e5";

    public JunitTest() throws IOException {
    }

    @Test
    public void createNewDiskTest() throws IOException {
        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(
                                proxmoxClient.createNewDisk(
                                        nodename, 1002,
                                        "virtio", 11,
                                        "NFS-DL380", 64
                                )
                        )
        );
    }

    @Test
    public void getTaskListTest() throws IOException {
        List<TaskInfo> taskInfoList = proxmoxClient.getNodeTaskInfoList("pve-e5");

        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(
                                taskInfoList
                        )
        );
    }

    @Test
    public void getTaskListTestRaw() throws IOException {
        PveClient pveClient = new PveClient(
                "js-dx-1.dabaiyun.net",
                8706
        );
        pveClient.login("root", "BayMax10281028");

        PveResult pveResult = pveClient.getNodes().get("pve-e5")
                .getTasks().nodeTasks();

        System.out.println(
                pveResult.getResponse().toString()
        );
    }

    @Test
    public void getTaskInfoTest() throws IOException {
        TaskInfo taskInfo = proxmoxClient.getNodeTaskInfo("pve-e5","UPID:pve-e5:000F6821:01BEC4D9:66C7A6D5:vzdump::root@pam:" );

        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(taskInfo)
        );
    }

    @Test
    public void getTaskInfoTestRaw() throws IOException {
        PveClient pveClient = new PveClient(
                "js-dx-1.dabaiyun.net",
                8706
        );
        pveClient.login("root", "BayMax10281028");

        PveResult pveResult = pveClient.getNodes().get("pve-e5")
                .getTasks().get("UPID:pve-e5:000F6821:01BEC4D9:66C7A6D5:vzdump::root@pam:")
                .getStatus().readTaskStatus();

        System.out.println(
                pveResult.getResponse().toString()
        );
    }

    @Test
    public void backupTestRaw() throws IOException {
        PveClient pveClient = new PveClient(
                "js-dx-1.dabaiyun.net",
                8706
        );
        pveClient.login("root", "BayMax10281028");

//        PveResult pveResult = pveClient.getNodes().get("pve-e5")
//                .getStorage().get("HDD-Raid6")
//                .getContent().get(StorageContent.ContentType_BACKUP)
//                .info();
        PveResult pveResult = pveClient.getNodes().get("pve-e5")
                .getQemu().createVm(
                        1007, null, null, null, null,
                        "HDD-Raid6:backup/vzdump-qemu-1007-2024_08_14-05_20_59.vma.zst", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        true, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null
                );

        //读取结果并转换到list
        System.out.println(
                pveResult.getResponse().toString()
        );
    }

    @Test
    public void setCloudInitUserPasswordTset() throws IOException {
        PveClient pveClient = new PveClient(
                "js-dx-1.dabaiyun.net",
                8706
        );
        pveClient.login("root", "BayMax10281028");

        String username = "root";
        String password = "Jjs_20020226&";

        password = URLEncoder.encode(password, StandardCharsets.UTF_8);

        System.out.println("EncodedPassword: " + password);

        PveResult pveResult = null;
        try {
            pveResult = pveClient.getNodes().get(nodename)
                    .getQemu().get(1008)
                    .getConfig().updateVm(
                            null, null, null, null, null, null, null, null, null, null, null, null, null,
                            password, null, username,
                            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                    );
        } catch (IOException e) {
            System.out.println(
                    new ObjectMapper().writerWithDefaultPrettyPrinter()
                            .writeValueAsString(e)
            );
        }

        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(pveResult)
        );
    }

    @Test
    public void diskTest() throws IOException {
        PveClient pveClient = new PveClient(
                "js-dx-1.dabaiyun.net",
                8706
        );
        pveClient.login("root", "BayMax10281028");

        PveResult pveResult = pveClient.getNodes().get(nodename)
                .getQemu().get(1008).getResize()
                .resizeVm("virtio0", "50G");
        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(
                                pveResult.getResponse()
                        )
//                pveResult.getResponse()
        );


    }

    @Test
    public void getVmConfigTest() throws IOException {
        VMConfig vmConfig = proxmoxClient.getVmConfig("pve-e5", 1002);
        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(vmConfig)
        );
    }

    @Test
    public void getVmConfigTestRawData() throws IOException {
        PveClient pveClient = new PveClient(
                "js-dx-1.dabaiyun.net",
                8706
        );
        pveClient.login("root", "BayMax10281028");
        PveResult pveResult = pveClient.getNodes().get(nodename).getQemu().get(202)
                .getConfig().vmConfig();
        System.out.println(pveResult.getResponse().toString());
    }

    @Test
    public void cloneTest() throws IOException {
        String upid = proxmoxClient.cloneNewVm(
                nodename,
                102,
                1005,
                "nvme0n1p1",
                "test-error",
                "create-test"
        );
        System.out.println(
                upid
        );
    }

    @Test
    public void logTest() {
        String input = "INFO: creating vzdump archive '/var/lib/vz/dump/vzdump-qemu-104-2024_04_21-14_53_32.vma.zst'";
        String regex = "'([^']*)'"; // 这个正则表达式会匹配两个单引号之间的所有非单引号字符

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String filepath = matcher.group(1); // group(1)捕获的是第一个括号内的内容
            System.out.println("捕获到的内容: " + filepath);
        } else {
            System.out.println("没有找到匹配的内容");
        }
    }

    @Test
    public void backupTest() throws IOException {
        PveClient pveClient = new PveClient(
                "192.168.77.8",
                8006
        );
        pveClient.login(
                "root",
                "BayMax10281028"
        );
        // UPID:pve-i5:003603CE:0785EA41:6624B7EC:vzdump:104:root@pam:
//        PveResult pveResult = pveClient.getNodes().get("pve-i5").getTasks()
//                .nodeTasks();
//                .get("UPID:pve-i5:003603CE:0785EA41:6624B7EC:vzdump:104:root@pam:")
//                .getStatus().readTaskStatus();
//        .getLog().readTaskLog();

        // 发起备份请求参数
        // storage=local
        // vmid=104
        // mode=snapshot
        // remove=0
        // compress=zstd
        // notes-template=%7B%7Bguestname%7D%7D

        PveResult pveResult = pveClient.getNodes().get("pve-i5")
                .getVzdump().vzdump(
                        null, null,
                        "zstd", null, null, null, null, null, null, null, null,
                        "snapshot",
                        "%7B%7Bguestname%7D%7D-with-protected", null, null, null,
                        true, null, null,
                        false, null, null, null, null, null,
                        "local", null,
                        "104", null
                );
        System.out.println(
                pveResult.getResponse().toString()
        );
    }

    @Test
    public void storageContentTest() throws IOException {
        List<StorageContent> storageContentList =
                proxmoxClient.getStorageContentList(
                        "pve-e5",
                        "HDD-Raid6",
                        StorageContent.ContentType_ISO
                );
        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(
                                storageContentList
                        )
        );
    }

    @Test
    public void isoImageTest() throws IOException {
        PveClient pveClient = new PveClient(
                "192.168.77.8",
                8006
        );
        pveClient.login(
                "root",
                "BayMax10281028"
        );
        PveResult pveResult = pveClient.getNodes().get("pve-i5")
                .getStorage().get("local").getContent()
                .index();
//                .get("iso").info();
        System.out.println(
                pveResult.getResponse().toString()
        );
    }

    @Test
    public void storageTest() throws IOException {
//        PveClient pveClient = new PveClient(
//                "192.168.77.7",
//                8006
//        );
//        pveClient.login(
//                "root",
//                "BayMax10281028"
//        );
//        PveResult pveResult = pveClient.getNodes().get("pve-e5")
//                .getStorage().get("local").getStatus().readStatus();
//        System.out.println(
//                pveResult.getResponse().toString()
//        );
        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(
                                proxmoxClient.getNodeStorageInfo(nodename, "HDD-Raid6")
                        )
        );
    }

    @Test
    public void nodeStatusTest() throws IOException {
        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(
                                proxmoxClient.getNodeStatus(nodename)
                        )
        );
    }

    @Test
    public void isPveUserExistTest() throws IOException {
        String username = "KephiHan@pve";
        System.out.println(
                proxmoxClient.isPveUserExist(username)
        );
    }

    @Test
    public void commonTest() throws IOException {
        PveClient pveClient = new PveClient(
                "192.168.77.7",
                8006
        );
        pveClient.login(
                "root",
                "BayMax10281028"
        );
        System.out.println(
                pveClient.getNodes().get("pve-e5")
                        .getStatus().status()
                        .getResponse().toString()
        );

    }

    @Test
    public void setCdromTest() throws IOException {
        System.out.println(
                proxmoxClient.setVmCDROM(
                        "pve-129k",
                        1010,
                        "local",
                        "cn_windows_server_2016_x64_dvd_9718765_virtio.iso"
                )
        );
    }

    @Test
    public void vmConfigTest() throws IOException {
        VMConfig vmConfig = proxmoxClient.getVmConfig(
                "pve-e5",
                1007
        );
        System.out.println(
                vmConfig.getBootOrder().get(0)
        );
        System.out.println(
                vmConfig.getDiskConfigMap()
                        .get(vmConfig.getBootOrder().get(0))
                        .getDiskSizeString()
        );
    }

    @Test
    public void resizeDisk() throws IOException {
        System.out.println(
                proxmoxClient.resizeDisk(
                        "pve-129k",
                        1002,
                        "scsi0",
                        40
                )
        );
    }

    @Test
    public void urlEncoderTest() {
        String net0config = "virtio,bridge=vmbr2";
        String b = URLEncoder.encode(net0config, StandardCharsets.UTF_8);
        System.out.println(b);
    }

    @Test
    public void setVmCloudInitIpConfig() throws IOException {
//        System.out.println(
//                proxmoxClient.get_ticketPVEAuthCookie()
//        );
//        System.out.println(
//                proxmoxClient.get_ticketCSRFPreventionToken()
//        );
        System.out.println(
                proxmoxClient.setVmCloudInitIpConfig(
                        "pve-e5",
                        501,
                        1,
                        "static",
                        "192.168.77.65",
                        24,
                        "192.168.77.1",
                        "auto",
                        "",
                        0,
                        ""
                )
        );
    }

    @Test
    public void setVmNetCard() throws IOException {
        System.out.println(
                proxmoxClient.setVmNetCard(
                        "pve-e5",
                        501,
                        4,
                        "virtio",
                        "vmbr0"
                )
        );
    }

    @Test
    public void unlinkHardwareTest() throws IOException {
        System.out.println(
                proxmoxClient.unlinkHardware(
                        "pve-e5",
                        1002,
                        "unused0"
                )
        );

    }

    @Test
    public void deleteHardwareTest() throws IOException {
        System.out.println(
                proxmoxClient.deleteHardware(
                        "pve-e5",
                        1002,
                        "scsi0"
                )
        );
    }

    @Test
    public void moveVMDiskTest() throws IOException {
        System.out.println(
                proxmoxClient.moveDisk(
                        "pve-i3",
                        9001,
                        "scsi0",
                        201,
                        "scsi0"
                )
        );
    }

    @Test
    public void getVmStatusTest() throws IOException {
        System.out.println(
                new ObjectMapper().writerWithDefaultPrettyPrinter()
                        .writeValueAsString(
                                proxmoxClient.getVmConfig(
                                        "pve-i3",
                                        9001
                                )
                        )
        );
    }

    @Test
    public void setBootDiskTest() throws IOException {
        VMConfig vmConfig = proxmoxClient.getVmConfig("pve-i3", 201);
        List<String> orderList = new ArrayList<>();
//        orderList.add("scsi0");
//        orderList.add("net0");
        System.out.println(
                proxmoxClient.setBootDisk(
                        "pve-i3", 201, vmConfig.getDigest(), orderList
                )
        );
    }

    @Test
    public void ipCidrList() throws IOException {
        proxmoxClient.getVmIpCidrList("pve-e5", 501, "ipfilter-net0")
                .forEach(System.out::println);
    }

    @Test
    public void ipSetList() throws IOException {
        proxmoxClient.getVmIpSetList("pve-e5", 501)
                .forEach(System.out::println);
    }

    @Test
    public void createIpSetList() throws IOException {
        System.out.println(
                proxmoxClient.createVmIpSet(
                        "pve-e5",
                        501,
                        "ipfilter-net2",
                        "这是一个备注"
                )
        );
    }

    @Test
    public void deleteIpSetTest() throws IOException {
        System.out.println(
                proxmoxClient.deleteVmIpSet(
                        "pve-e5",
                        501,
                        "ipfilter-net2"
                )
        );
    }

    @Test
    public void createVmIpCidr() throws IOException {
        System.out.println(
                proxmoxClient.createVmIpCidr(
                        "pve-e5",
                        501,
                        "ipfilter-net1",
                        "10.1.0.98/24",
                        "备注123",
                        false
                )
        );
    }

    @Test
    public void deleteVmIpCidr() throws IOException {
        System.out.println(
                proxmoxClient.deleteVmIpCidr(
                        "pve-e5",
                        501,
                        "ipfilter-net1",
                        "10.1.0.0/24"
                )
        );
    }

    @Test
    public void rrdDataTest() throws IOException {
        List<RrdData> rrdDataList = proxmoxClient.getVmHistoryPerformanceData("pve-e5", 1003);
        rrdDataList.forEach(System.out::println);
    }

    @Test
    public void getVersionTest() throws IOException {
        System.out.println(proxmoxClient.getProxmoxVersion("pve-e5"));
    }

    @Test
    public void wrongCookieTest() {
//        ProxmoxClient proxmoxClient = new ProxmoxClient(
//                "10.0.16.8",
//                8006,
//                "root",
//                "password"
//        );
//        System.out.println(proxmoxClient.getProxmoxVersion("pve-e5"));
//
//        String hostname = proxmoxClient.getHostname();
//        int port = proxmoxClient.getPort();
//        String _ticketPVEAuthCookie = proxmoxClient.get_ticketPVEAuthCookie();
//        String _ticketCSRFPreventionToken = proxmoxClient.get_ticketCSRFPreventionToken();
//
//        System.out.println("hostname : " + hostname);
//        System.out.println("port : " + port);
//        System.out.println("_ticketPVEAuthCookie : " + _ticketPVEAuthCookie);
//        System.out.println("_ticketCSRFPreventionToken : " + _ticketCSRFPreventionToken);

        ProxmoxClient proxmoxClient_new = new ProxmoxClient(
                "123",
                "456",
                "10.0.16.8",
                8006
        );

        try {
            System.out.println(proxmoxClient_new.getNodeStatus("pve-e5"));
        } catch (IOException e) {
            System.out.println("Message: " + e.getMessage());
            if (e.getMessage().contains("code: 401")) {
                System.out.println("认证错误");
            }
        }

    }

    @Test
    public void getNodeStatus() throws IOException {

    }

    @Test
    public void checkAuthValid() {
        ProxmoxClient proxmoxClient_new = new ProxmoxClient(
                "123",
                "456",
                "10.0.16.8",
                8006
        );
        System.out.println(proxmoxClient_new.checkCookieValid());
    }

    @Test
    public void getPveUser() throws IOException {
        System.out.println(
                proxmoxClient.getPveUser("wuzhehan@pve")
        );

    }

    @Test
    public void getPveUserList() throws IOException {
        System.out.println(
                proxmoxClient.getPveUserList().toString()
        );
    }

    @Test
    public void isPveUserExist() throws IOException {
        System.out.println(
                proxmoxClient.isPveUserExist("wa")
        );
    }

    @Test
    public void changeProxmoxUserPassword() throws IOException {
        System.out.println(
                proxmoxClient.changeProxmoxUserPassword(
                        "wuzhehan",
                        "aA77777"
                )
        );
    }

    @Test
    public void findAvailableVmid() throws IOException {
        System.out.println(
                proxmoxClient.findAvailableVmid("pve-e5", 1001, 1999)
        );
    }

//    @Test
//    public void getUserRole() throws IOException {
//        System.out.println(
//                proxmoxClient.getUserRole("DABAIYUNUser")
//        );
//    }

    @Test
    public void getUserRoleList() throws IOException {
        List<UserRole> userRoleList = proxmoxClient.getUserRoleList();
        Map<String, Map<String, Set<String>>> roleMap = new HashMap<>();
        for (UserRole userRole : userRoleList) {
            if (userRole.getPrivs().equals("")) {
                continue;
            }
            String[] roleNames = userRole.getPrivs().split(",");
            for (String roleName : roleNames) {
                String[] strings = roleName.split("\\.");

                if (strings.length == 2) {
                    String group = strings[0];
                    String role = strings[1];
                    if (roleMap.containsKey(group)) {
                        roleMap.get(group).put(role, null);
                    } else {
                        Map<String, Set<String>> secondMap = new HashMap<>();
                        secondMap.put(role, null);
                        roleMap.put(group, secondMap);
                    }
                } else if (strings.length == 3) {
                    String group = strings[0];
                    String role = strings[1];
                    String subRole = strings[2];
                    if (roleMap.containsKey(group)) {
                        if (roleMap.get(group).containsKey(role) && roleMap.get(group).get(role) != null) {
                            roleMap.get(group).get(role).add(subRole);
                        } else {
                            Set<String> subRoleSet = new HashSet<>();
                            subRoleSet.add(subRole);
                            roleMap.get(group).put(role, subRoleSet);
                        }
                    } else {
                        Map<String, Set<String>> secondMap = new HashMap<>();
                        Set<String> subRoleSet = new HashSet<>();
                        subRoleSet.add(subRole);
                        secondMap.put(role, subRoleSet);
                        roleMap.put(group, secondMap);
                    }
                }
            }
        }

        for (String group : roleMap.keySet()) {
            System.out.println(group + ":");
            Map<String, Set<String>> secondMap = roleMap.get(group);
            for (String role : secondMap.keySet()) {
                Set<String> subRoleSet = secondMap.get(role);
                if (subRoleSet == null) {
                    System.out.println("  " + role + ",");
                } else {
                    System.out.println("  " + role + ":");
                    for (String subRole : subRoleSet) {
                        System.out.println("    " + subRole + ",");
                    }
                }
            }
        }

        for (String group : roleMap.keySet()) {
            println("public enum " + group + " {");

            Map<String, Set<String>> secondMap = roleMap.get(group);
            for (String role : secondMap.keySet()) {
                Set<String> subRoleSet = secondMap.get(role);
                if (subRoleSet == null) {
                    println(role + "(\"" + group + "." + role + "\")" + ",");
                } else {
                    println("public enum " + role + " {");
                    for (String subRole : subRoleSet) {
                        println(subRole + "(\"" + group + "." + role + "." + subRole + "\")" + ",");
                    }
                    println("}");
                }
            }
            println("}");
        }


    }

    private void print(String s) {
        System.out.print(s);
    }

    private void println(String s) {
        System.out.println(s);
    }
}
