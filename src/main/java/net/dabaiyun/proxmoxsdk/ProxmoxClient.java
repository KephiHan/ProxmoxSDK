package net.dabaiyun.proxmoxsdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.corsinvest.proxmoxve.api.PveClient;
import it.corsinvest.proxmoxve.api.PveResult;
import lombok.SneakyThrows;
import net.dabaiyun.proxmoxsdk.entity.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ProxmoxClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    //PVE客户端
    private PveClient pveClient;

    public ProxmoxClient(
            String hostname,
            int port,
            String username,
            String password
    ) throws IOException {
        //初始化PVE客户端
        this.pveClient = new PveClient(hostname, port);
        pveClient.login(username, password);
    }

    public ProxmoxClient(
            String _ticketCSRFPreventionToken,
            String _ticketPVEAuthCookie,
            String _hostname,
            int _port
    ) {
        //从Cookie序列化客户端
        this.pveClient = new PveClient(_ticketCSRFPreventionToken, _ticketPVEAuthCookie, _hostname, _port);
    }

    public String getHostname() {
        return pveClient.getHostname();
    }

    public int getPort() {
        return pveClient.getPort();
    }

    public String get_ticketPVEAuthCookie() {
        return pveClient.get_ticketPVEAuthCookie();
    }

    public String get_ticketCSRFPreventionToken() {
        return pveClient.get_ticketCSRFPreventionToken();
    }


    //////////////////////////////////////////
    //                                      //
    //             管理功能方法               //
    //                                      //
    //////////////////////////////////////////

    /**
     * 读取节点版本
     *
     * @param nodeName 节点名称
     * @return 版本号
     * @throws IOException e
     */
    public String getProxmoxVersion(String nodeName) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getVersion().version();
        return pveResult.getResponse().getJSONObject("data").getString("version");
    }

    /**
     * 获取节点状态
     *
     * @param nodeName 节点名称
     * @return 节点状态数据
     * @throws IOException e
     */
    public NodeStatus getNodeStatus(String nodeName) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getStatus().status();
        return objectMapper.readValue(
                pveResult.getResponse().getJSONObject("data").toString(),
                new TypeReference<NodeStatus>() {
                }
        );
    }

    /**
     * 获取节点存储列表
     *
     * @param nodeName 节点名称
     * @return 存储列表
     */
    public List<StorageInfo> getNodeStorageList(String nodeName) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getStorage().index();
        return objectMapper.readValue(
                pveResult.getResponse().getJSONArray("data").toString(),
                new TypeReference<List<StorageInfo>>() {
                }
        );
    }

    /**
     * 获取存储详情
     *
     * @param nodeName    节点名称
     * @param storageName 存储名称
     * @return 存储详情
     */
    public StorageInfo getNodeStorageInfo(String nodeName, String storageName) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getStorage().get(storageName).getStatus().readStatus();
        StorageInfo storageInfo = objectMapper.readValue(
                pveResult.getResponse().getJSONObject("data").toString(),
                new TypeReference<StorageInfo>() {}
        );
        storageInfo.setStorage(storageName);
        return storageInfo;
    }

//    public List<TaskInfo> getNodeTaskInfoList(String nodeName,
//                                              Boolean errorOnly,
//                                              Integer limit,
//                                              Integer sinceUnixTimeStrap,
//                                              String source,
//                                              Integer vmid
//    ){
//        PveResult pveResult = pveClient.getNodes().get(nodeName)
//
//    }

    /**
     * 读取节点上的任务列表
     * 默认列出50条
     *
     * @param nodeName 节点
     * @return 任务列表
     * @throws IOException e
     */
    public List<TaskInfo> getNodeTaskInfoList(String nodeName) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getTasks().nodeTasks();
        return objectMapper.readValue(
                pveResult.getResponse().getJSONArray("data").toString(),
                new TypeReference<List<TaskInfo>>() {}
        );
    }

    /**
     * 读取节点上指定的任务详情
     *
     * @param nodeName 节点
     * @param upid     任务ID
     * @return 任务详情
     * @throws IOException e
     */
    public TaskInfo getNodeTaskInfo(String nodeName, String upid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getTasks().get(upid).getStatus().readTaskStatus();
        return objectMapper.readValue(
                pveResult.getResponse().getJSONObject("data").toString(),
                new TypeReference<TaskInfo>() {}
        );
    }


    /**
     * 检查认证cookie是否有效
     *
     * @return 是否有效
     */
    public boolean checkCookieValid() {
        try {
            pveClient.getNodes().index();
        } catch (IOException e) {
            //检查是否包含code : 401
            if (e.getMessage().contains("code: 401")) {
                return false;
            }
        }
        return true;
    }


    //////////////////////////////////////////
    //                                      //
    //             VM操作方法                //
    //                                      //
    //////////////////////////////////////////

    /**
     * 克隆VM
     *
     * @param nodeName      节点
     * @param sourceVmid    克隆源VM
     * @param targetVmid    目标VMID
     * @param targetStorage 目标存储
     * @param vmName        名字
     * @param description   描述
     * @return PVE Task ID
     * @throws IOException e
     */
    public String cloneNewVm(String nodeName, int sourceVmid, int targetVmid, String targetStorage, String vmName, String description) throws IOException {
        PveResult cloneResult =
                pveClient.getNodes().get(nodeName)
                        .getQemu().get(sourceVmid).getClone()
                        .cloneVm(
                                targetVmid,
                                null,
                                description,
                                "qcow2",
                                true,
                                vmName,
                                null,
                                null,
                                targetStorage,
                                nodeName
                        );
        //读取任务ID
        return cloneResult.getResponse().getString("data");
    }

    /**
     * 为VPS添加用户访问权限
     *
     * @param vmid        目标VMid
     * @param pveUserName 授予的PVE用户
     * @return 成功返回true，失败抛异常
     */
    public boolean setPermission(int vmid, String pveUserName, String role) throws IOException {
        PveResult pveResult = pveClient.getAccess().getAcl().updateAcl(
                "/vms/" + vmid,
                role,
                false,
                null,
                null,
                null,
                pveUserName
        );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 备份恢复VM
     *
     * @param nodeName 节点名称
     * @param vmid     VMID
     * @param archive  备份文件的volid eg. HDD-Raid6:backup/vzdump-qemu-1007-2024_08_14-05_20_59.vma.zst
     * @return 恢复Job的UPID
     */
    public String restoreVm(String nodeName, int vmid, String archive) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().createVm(
                        vmid, null, null, null, null,
                        archive, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        true, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null
                );
        return pveResult.getResponse().getString("data");
    }


    //////////////////////////////////////////
    //                                      //
    //            数据操作方法                //
    //                                      //
    //////////////////////////////////////////

    /**
     * 获取VM状态
     *
     * @param nodeName 节点
     * @param vmid     vmid
     * @return VMStatus
     */
    public VMStatus getVmStatus(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid).getStatus().getCurrent().vmStatus();
        return objectMapper.readValue(
                pveResult.getResponse().getJSONObject("data").toString(),
                new TypeReference<VMStatus>() {
                }
        );
    }

    /**
     * 获取VM配置
     *
     * @param nodeName 节点
     * @param vmid     vmid
     * @return VMConfig
     * @throws JsonProcessingException e
     */
    public VMConfig getVmConfig(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid).getConfig().vmConfig();

        JSONObject dataJsonObject = pveResult.getResponse().getJSONObject("data");

        VMConfig vmConfig = objectMapper.readValue(
                dataJsonObject.toString(),
                new TypeReference<VMConfig>() {
                }
        );

        //BootOrder 如果字符串为空表示没有任何启动项
        if (!vmConfig.getBoot().trim().isBlank()) {
            String[] orderAndKeys = vmConfig.getBoot().split("=");
            if (orderAndKeys.length == 2) {
                String[] bootList = orderAndKeys[1].split(";");
                vmConfig.setBootOrder(List.of(bootList));
            }
        }

        //NetN
        for (int netN = 0; netN < 7; netN++) {
            //检查是否包含Net N
            if (dataJsonObject.has("net" + netN)) {
                //构造NetCardConfig
                VMConfig.NetConfig netConfig = new VMConfig.NetConfig();

                String configLine = dataJsonObject.getString("net" + netN);
                String[] configList = configLine.split(",");
                //第一个为网卡类型和mac
                String[] typeAndMac = configList[0].split("=");
                netConfig.setDeviceType(typeAndMac[0]);
                netConfig.setMac(typeAndMac[1]);
                //遍历每一个key=value
                for (int configListIndex = 1; configListIndex < configList.length; configListIndex++) {
                    String[] keyAndValue = configList[configListIndex].split("=");
                    String key = keyAndValue[0];
                    String value = keyAndValue[1];

                    switch (key) {
                        case "bridge" -> netConfig.setBridge(value);
                        case "firewall" -> netConfig.setFirewall(value.equals("1"));
                        case "link_down" -> netConfig.setLinkDown(value.equals("1"));
                        case "mtu" -> netConfig.setMtu(Integer.parseInt(value));
                        case "queues" -> netConfig.setMultiQueue(Integer.parseInt(value));
                        case "rate" -> netConfig.setLimitRateMBps(Integer.parseInt(value));
                        case "tag" -> netConfig.setVlanId(Integer.parseInt(value));
                    }

                }
                vmConfig.getNetDeviceMap().put(netN, netConfig);
            }
        }

        // Disk List
        //遍历查找所有存在的磁盘设备
        Set<String> diskDeviceSet = new HashSet<>();

        List<String> diskDeviceTypeList = new ArrayList<>();
        diskDeviceTypeList.add(VMConfig.DiskConfig.DeviceType_IDE);
        diskDeviceTypeList.add(VMConfig.DiskConfig.DeviceType_SATA);
        diskDeviceTypeList.add(VMConfig.DiskConfig.DeviceType_SCSI);
        diskDeviceTypeList.add(VMConfig.DiskConfig.DeviceType_VirtIO);
        diskDeviceTypeList.add(VMConfig.DiskConfig.DeviceType_Unused);

        for (String deviceType : diskDeviceTypeList) {
            for (String key : dataJsonObject.keySet()) {
                //排除scsihw
                if (key.equals("scsihw"))
                    continue;
                if (key.startsWith(deviceType)) {
                    diskDeviceSet.add(key);
                }
            }
        }

        for (String diskDevice : diskDeviceSet) {
            VMConfig.DiskConfig diskConfig = new VMConfig.DiskConfig();

            String configLine = dataJsonObject.getString(diskDevice);
            String[] configList = configLine.split(",");


            String storage = "";
            String folder = "";
            String filename = "";
            //检查是否是空CD ROM
            if ("none".equals(configList[0])) {
                storage = "none";
                folder = "none";
                filename = "none";
            } else {
                //存储:VMID/文件名
                String[] storageAndFilePath = configList[0].split(":");
                String[] vmidFolderAndFileName = storageAndFilePath[1].split("/");
                storage = storageAndFilePath[0];
                folder = vmidFolderAndFileName[0];
                filename = vmidFolderAndFileName[1];
            }

            String deviceTypeString = diskDevice;
            //去除设备名末尾数字
            char endChar = deviceTypeString.charAt(deviceTypeString.length() - 1);
            while (endChar >= '0' && endChar <= '9') {
                deviceTypeString = deviceTypeString.substring(0, deviceTypeString.length() - 1);
                endChar = deviceTypeString.charAt(deviceTypeString.length() - 1);
            }

            diskConfig.setDeviceType(deviceTypeString);
            diskConfig.setStorage(storage);
            diskConfig.setFolder(folder);
            diskConfig.setFilename(filename);

            //解析设备序号
            for (String type : diskDeviceTypeList) {
                if (diskDevice.startsWith(type)) {
                    String numberStr = diskDevice.replace(type, "");
                    diskConfig.setDeviceNumber(Integer.valueOf(numberStr));
                    break;
                }
            }

            for (int configListIndex = 1; configListIndex < configList.length; configListIndex++) {
                String[] keyAndValue = configList[configListIndex].split("=");
                String key = keyAndValue[0];
                String value = keyAndValue[1];

                switch (key) {
                    case "iothread" -> diskConfig.setIothread(value.equals("1"));
                    case "backup" -> diskConfig.setBackup(value.equals("1"));
                    case "discard" -> diskConfig.setDiscard(value.equals("on"));
                    case "replicate" -> diskConfig.setReplicate(value.equals("1"));
                    case "ro" -> diskConfig.setReadonly(value.equals("1"));
                    case "ssd" -> diskConfig.setSsd(value.equals("1"));
                    case "media" -> diskConfig.setCDROM(value.equals("cdrom"));
                    case "size" -> {
                        char sizeUnit = value.charAt(value.length() - 1);
                        long sizeNumber = Long.parseLong(value.substring(0, value.length() - 1));
                        //根据不同单位，转换到Bytes
                        long sizeBytes = 0L;

                        switch (sizeUnit) {
                            case 'T' -> sizeBytes = (long) (sizeNumber * Math.pow(1024, 4));
                            case 'G' -> sizeBytes = (long) (sizeNumber * Math.pow(1024, 3));
                            case 'M' -> sizeBytes = (long) (sizeNumber * Math.pow(1024, 2));
                            case 'K' -> sizeBytes = (long) (sizeNumber * Math.pow(1024, 1));
                            case 'B' -> sizeBytes = (long) (sizeNumber * Math.pow(1024, 0));
                        }

                        diskConfig.setSizeBytes(sizeBytes);
                    }
                }
            }

            vmConfig.getDiskConfigMap().put(diskDevice, diskConfig);
        }


        return vmConfig;
    }

    /**
     * 获取所有VM信息
     *
     * @param nodeName 节点名
     * @return VMinfo List
     * @throws IOException ex
     */
    public List<VMInfo> getVMInfoList(String nodeName) throws IOException {
        //读取所有vm信息
        PveResult vmlistResult = pveClient.getNodes().get(nodeName)
                .getQemu().vmlist();
        //读取结果并转换到list
        return objectMapper.readValue(
                vmlistResult.getResponse().getJSONArray("data").toString(),
                new TypeReference<List<VMInfo>>() {
                }
        );
    }

    /**
     * 读取指定节点指定存储下的指定类型数据
     *
     * @param nodeName    节点名称
     * @param storage     存储区
     * @param contentType 数据类型 enum: iso,image,backup
     * @return
     * @throws IOException
     */
    public List<StorageContent> getStorageContentList(String nodeName, String storage, String contentType) throws IOException {
        //读取指定节点指定存储下的指定类型数据
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getStorage().get(storage)
                .getContent().get(contentType)
                .info();
        //读取结果并转换到list
        return objectMapper.readValue(
                pveResult.getResponse().getJSONArray("data").toString(),
                new TypeReference<List<StorageContent>>() {
                }
        );
    }

    /**
     * 获取所有存在的虚拟机VMID SET
     *
     * @param nodeName 节点
     * @return VMID SET
     */
    public Set<Integer> getVmidSet(String nodeName) throws IOException {
        //获取所有VM信息
        List<VMInfo> vmInfoList = this.getVMInfoList(nodeName);
        //建立vmid SET
        Set<Integer> vmidSet = new HashSet<>();
        for (VMInfo vmInfo : vmInfoList) {
            vmidSet.add(vmInfo.getVmid());
        }
        return vmidSet;
    }

    /**
     * 获取VM性能数据历史记录
     *
     * @param nodeName 节点
     * @param vmid     VMID
     * @return 历史数据列表
     */
    public List<RrdData> getVmHistoryPerformanceData(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getRrddata().rrddata("hour", "AVERAGE");
        return objectMapper.readValue(
                pveResult.getResponse().get("data").toString(),
                new TypeReference<List<RrdData>>() {
                }
        );
    }

    /**
     * 查询是否存在用户
     *
     * @param pveUsername 用户名
     * @return 是否存在
     * @throws IOException ex
     */
    public boolean isPveUserExist(String pveUsername) throws IOException {
        if (!pveUsername.endsWith("@pve")) {
            pveUsername = pveUsername + "@pve";
        }
        List<PveUser> pveUserList = this.getPveUserList();
        for (PveUser pveUser : pveUserList) {
            if (pveUser.getUserid().equals(pveUsername)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     * @throws IOException ex
     */
    public List<PveUser> getPveUserList() throws IOException {
        PveResult pveResult = pveClient.getAccess().getUsers()
                .index();
        return objectMapper.readValue(
                pveResult.getResponse().getJSONArray("data").toString(),
                new TypeReference<List<PveUser>>() {
                }
        );
    }

    /**
     * 根据用户名查询用户
     *
     * @param pveUsername pve用户名
     * @return 用户数据实体
     * @throws IOException ex
     */
    public PveUser getPveUser(String pveUsername) throws IOException {
        if (!pveUsername.endsWith("@pve")) {
            pveUsername = pveUsername + "@pve";
        }
        PveResult pveResult = pveClient.getAccess().getUsers()
                .get(pveUsername).readUser();
        PveUser pveUser = objectMapper.readValue(
                pveResult.getResponse().getJSONObject("data").toString(),
                new TypeReference<PveUser>() {
                }
        );
        pveUser.setUserid(pveUsername);
        return pveUser;
    }

    /**
     * 创建新的PVE用户账户
     *
     * @param userName 用户名（不带@pve）
     * @param password 密码
     * @param email    邮箱
     * @return 成功？
     * @throws IOException e
     */
    public boolean createProxmoxUser(String userName, String password, String email) throws IOException {
        String pveUsername = userName;
        if (!pveUsername.endsWith("@pve")) {
            pveUsername = pveUsername + "@pve";
        }
        PveResult pveResult = pveClient.getAccess().getUsers().createUser(
                pveUsername,
                userName,
                email,
                true,
                0,
                null,
                null,
                null,
                null,
                password
        );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 更新用户信息
     *
     * @param userName
     * @param password
     * @param email
     * @return
     * @throws IOException
     */
    public boolean updateProxmoxUser(String userName, String password, String email) throws IOException {
        String pveUsername = userName;
        if (!pveUsername.endsWith("@pve")) {
            pveUsername = pveUsername + "@pve";
        }
        PveResult pveResult = pveClient.getAccess().getUsers()
                .get(userName).updateUser(
                        false,
                        userName,
                        email,
                        true,
                        0,
                        null,
                        null,
                        null,
                        null
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 修改PveUser密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 修改成功
     * @throws IOException ex
     */
    public boolean changeProxmoxUserPassword(String userName, String password) throws IOException {
        String pveUsername = userName;
        if (!pveUsername.endsWith("@pve")) {
            pveUsername = pveUsername + "@pve";
        }
        PveResult pveResult = pveClient.getAccess().getPassword()
                .changePassword(password, pveUsername);
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 重配置CPU核心数和内存
     */
    public boolean configVmCpuRam(String nodeName, int vmid, int vCpuCores, int ramMbs) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        vCpuCores, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        ramMbs, null, null, null, null, null,
                        false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        1, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 向VM增加磁盘
     *
     * @param nodeName   节点
     * @param vmid       VMID
     * @param targetDisk 目标磁盘位置
     * @param sizeGb     目标大小
     * @return 成功？
     * @throws IOException ex
     */
    public boolean resizeDisk(String nodeName, int vmid, String targetDisk, int sizeGb) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getResize().resizeVm(targetDisk, sizeGb + "G");
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 创建新的磁盘
     *
     * @param nodeName     PVE节点名
     * @param vmid         VMID
     * @param deviceType   设备类型 ide sata scsi virtio
     * @param deviceNumber 设备序号
     * @param storage      存储区
     * @param sizeGb       磁盘大小 GB
     * @return success?
     * @throws IOException e
     */
    public boolean createNewDisk(
            String nodeName, int vmid,
            String deviceType, int deviceNumber,
            String storage, int sizeGb
    ) throws IOException {
        return this.createNewDisk(
                nodeName, vmid,
                deviceType, deviceNumber,
                storage, sizeGb, "qcow2",
                true, true, false,
                true, false, false
        );
    }

    /**
     * 创建新的磁盘
     *
     * @param nodeName     PVE节点名
     * @param vmid         VMID
     * @param deviceType   设备类型 ide sata scsi virtio
     * @param deviceNumber 设备序号
     * @param storage      存储区
     * @param sizeGb       磁盘大小 GB
     * @param format       格式 raw vmdk qcow2
     * @param iothread     开启独立IO线程
     * @param backup       备份
     * @param discard      关机后丢弃修改
     * @param replicate    参与复制
     * @param readonly     只读
     * @param ssd          固态硬盘仿真
     * @return success?
     * @throws IOException e
     */
    public boolean createNewDisk(
            String nodeName, int vmid,
            String deviceType, int deviceNumber,
            String storage, int sizeGb, String format,
            boolean iothread, boolean backup, boolean discard,
            boolean replicate, boolean readonly, boolean ssd
    ) throws IOException {
        //初始化磁盘配置文本
        StringBuilder configLineBuilder = new StringBuilder();
        configLineBuilder
                .append(storage).append(":").append(sizeGb)
                .append(",").append("format=").append(format);
        if (iothread)
            configLineBuilder.append(",").append("iothread=").append("on");
        if (!backup)
            configLineBuilder.append(",").append("backup=").append("0");
        if (discard)
            configLineBuilder.append(",").append("discard=").append("on");
        if (!replicate)
            configLineBuilder.append(",").append("replicate=").append("no");
        if (readonly)
            configLineBuilder.append(",").append("ro=").append("on");
        if (ssd)
            configLineBuilder.append(",").append("ssd=").append("on");
        //转码UrlCode
        String encodedConfigLine = URLEncoder.encode(configLineBuilder.toString(), StandardCharsets.UTF_8);
        //参数MAP
        Map<Integer, String> diskMap = new HashMap<>();
        diskMap.put(deviceNumber, encodedConfigLine);
        //根据不同的设备类型，参数放在不同位置
        switch (deviceType) {
            case VMConfig.DiskConfig.DeviceType_IDE -> {
                PveResult pveResult = pveClient
                        .getNodes().get(nodeName)
                        .getQemu().get(vmid)
                        .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null, null, null, null, null, null, null, null,
                                diskMap, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null,
                                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null, null, null, null
                        );
                return pveResult.isSuccessStatusCode();
            }
            case VMConfig.DiskConfig.DeviceType_SATA -> {
                PveResult pveResult = pveClient
                        .getNodes().get(nodeName)
                        .getQemu().get(vmid)
                        .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null, null, null, null, null, null, null, null,
                                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                diskMap,
                                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null, null, null, null
                        );
                return pveResult.isSuccessStatusCode();
            }
            case VMConfig.DiskConfig.DeviceType_SCSI -> {
                PveResult pveResult = pveClient
                        .getNodes().get(nodeName)
                        .getQemu().get(vmid)
                        .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null, null, null, null, null, null, null, null,
                                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null,
                                diskMap, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null, null, null, null
                        );
                return pveResult.isSuccessStatusCode();
            }
            case VMConfig.DiskConfig.DeviceType_VirtIO -> {
                PveResult pveResult = pveClient
                        .getNodes().get(nodeName)
                        .getQemu().get(vmid)
                        .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null, null, null, null, null, null, null, null,
                                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                null,
                                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                                diskMap, null, null, null
                        );
                return pveResult.isSuccessStatusCode();
            }
            default -> throw new IllegalStateException("Unexpected device type: " + deviceType);
        }
    }

    /**
     * 更换VM 虚拟光驱
     *
     * @param nodeName 节点
     * @param vmid     VMID
     * @return 成功？
     * @throws IOException ex
     */
    public boolean setVmCDROM(String nodeName, int vmid, String storage, String filename) throws IOException {
        String filePath = storage + ":iso/" + filename;
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null,
                        filePath, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 弹出VM 虚拟光驱
     *
     * @param nodeName
     * @param vmid
     * @return 成功?
     * @throws IOException
     */
    public boolean plugOutVmCDROM(String nodeName, int vmid) throws IOException {
        String configLine = "none,media=cdrom";
        String encodedConfigLine = URLEncoder.encode(configLine, StandardCharsets.UTF_8);
//        VMConfig vmConfig = this.getVmConfig(nodeName, vmid);
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null,
                        encodedConfigLine, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 设置VM的os类型
     *
     * @param nodeName  节点
     * @param vmid      VMID
     * @param pveOsType PVE系统类型
     * @return 成功？
     * @throws IOException ex
     */
    public boolean setPveOsType(String nodeName, int vmid, String pveOsType) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        pveOsType, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 修改VM显示名称
     *
     * @param nodeName 节点
     * @param vmid     VMID
     * @param name     新名称
     * @return 成功？
     */
    public boolean setVMName(String nodeName, int vmid, String name) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        name, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * VM设置网卡 快速设置默认值
     *
     * @param nodeName      节点
     * @param vmid          VMID
     * @param netN          网卡序号，从0开始，如第二张网卡为1
     * @param netDeviceType 网卡类型
     * @param bridgeName    网桥
     * @return 成功？
     */
    public boolean setVmNetCard(
            String nodeName,
            int vmid,
            int netN,
            String netDeviceType,
            String bridgeName
    ) throws IOException {
        return this.setVmNetCard(
                nodeName,
                vmid,
                netN,
                netDeviceType,
                null,
                bridgeName,
                false,
                false,
                0,
                0,
                0,
                0
        );
    }

    /**
     * VM设置网卡
     *
     * @param nodeName      节点
     * @param vmid          VMID
     * @param netN          网卡序号，从0开始，如第二张网卡为1
     * @param netDeviceType 网卡类型
     * @param mac           MAC地址
     * @param bridgeName    网桥
     * @param firewall      开启防火墙
     * @param linkDown      断开
     * @param rate          限速(MB/s) 0=不限
     * @param mtu           MTU 0表示默认，1表示继承bridge的mtu
     * @param multiQueue    多队列深度，0表示不启用
     * @param vlanId        vlan ID
     * @return 成功？
     */
    public boolean setVmNetCard(
            String nodeName,
            int vmid,
            int netN,
            String netDeviceType,
            String mac,
            String bridgeName,
            boolean firewall,
            boolean linkDown,
            int rate,
            int mtu,
            int multiQueue,
            int vlanId
    ) throws IOException {
        StringBuilder netNconfigStringBuilder = new StringBuilder();
        netNconfigStringBuilder
                .append(netDeviceType)
                .append(mac != null ? "=" + mac : "")
                .append(",bridge=")
                .append(bridgeName)
                .append(firewall ? ",firewall=1" : "")
                .append(linkDown ? ",link_down=1" : "")
                .append(rate != 0 ? ",rate=" + rate : "")
                .append(mtu != 0 ? ",mtu=" + mtu : "")
                .append(multiQueue != 0 ? ",queues=" + multiQueue : "")
                .append(vlanId != 0 ? ",tag=" + vlanId : "");

        String netNconfigString = netNconfigStringBuilder.toString();
        String encodedConfigString = URLEncoder.encode(netNconfigString, StandardCharsets.UTF_8);
        Map<Integer, String> netNMap = new HashMap<>();
        netNMap.put(netN, encodedConfigString);
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        netNMap, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 移除VM硬件设备
     *
     * @param nodeName 节点
     * @param vmid     VMID
     * @return success
     */
    public boolean unlinkHardware(String nodeName, int vmid, String hardwareName) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getUnlink().unlink(hardwareName);
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 直接删除VM硬件设备
     *
     * @param nodeName 节点
     * @param vmid     VMID
     * @return success
     */
    public boolean deleteHardware(String nodeName, int vmid, String hardwareName) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getUnlink().unlink(hardwareName, true);
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 重新分配磁盘给另一个VM
     *
     * @param nodeName   节点名
     * @param sourceVmid 源vmid
     * @param sourceDisk 源磁盘设备号
     * @param targetVmid 目标VMID
     * @param targetDisk 目标磁盘设备号
     * @return UPID
     * @throws IOException e
     */
    public String moveDisk(String nodeName, int sourceVmid, String sourceDisk, int targetVmid, String targetDisk) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(sourceVmid)
                .getMoveDisk().moveVmDisk(
                        sourceDisk,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        targetDisk,
                        targetVmid
                );
        return pveResult.getResponse().getString("data");
    }

    /**
     * 设置引导磁盘
     *
     * @param nodeName  节点
     * @param vmid      VMID
     * @param digest    配置文件SHA1（从VMConfig获取）
     * @param orderList 引导顺序列表
     * @return 设置成功
     * @throws IOException e
     */
    public boolean setBootDisk(String nodeName, int vmid, String digest, List<String> orderList) throws IOException {
        //生成引导序列配置文本行
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("order=");
        //遍历引导列表，构建配置文本行
        Iterator<String> orderIterator = orderList.iterator();
        while (orderIterator.hasNext()) {
            stringBuilder.append(orderIterator.next());
            if (orderIterator.hasNext()) {
                stringBuilder.append(";");
            }
        }

        String encodedBootLine = URLEncoder.encode(stringBuilder.toString(), StandardCharsets.UTF_8.name());

        if (orderList.isEmpty()) {
            encodedBootLine = " ";
        }

        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(
                        null, null, null, null, null, null, null, null, null,
                        encodedBootLine, null,
                        null, null, null, null, null, null, null,
                        null, null, null, null,
                        digest,
                        null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );

        return pveResult.isSuccessStatusCode();
    }

    /**
     * 获取用户权限列表
     *
     * @return 权限对象列表
     * @throws IOException ex
     */
    public List<UserRole> getUserRoleList() throws IOException {
        PveResult pveResult = pveClient.getAccess().getRoles().index();
        return objectMapper.readValue(
                pveResult.getResponse().getJSONArray("data").toString(),
                new TypeReference<List<UserRole>>() {
                }
        );
    }

    /**
     * 创建UserRole
     *
     * @param roleId    Role名称
     * @param privsList 权限列表
     * @return 创建结果
     * @throws IOException ex
     */
    public boolean createUserRole(String roleId, List<String> privsList) throws IOException {
        StringBuilder privs = new StringBuilder();
        for (String priv : privsList) {
            privs.append(priv).append(",");
        }
        PveResult pveResult = pveClient.getAccess().getRoles()
                .createRole(
                        roleId,
                        privs.toString()
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 更新UserRole
     *
     * @param roleId    Role名称
     * @param privsList 权限列表
     * @return 更新结果
     * @throws IOException ex
     */
    public boolean updateUserRole(String roleId, List<String> privsList) throws IOException {
        StringBuilder privs = new StringBuilder();
        for (String priv : privsList) {
            privs.append(priv).append(",");
        }
        PveResult pveResult = pveClient.getAccess()
                .getRoles().get(roleId)
                .updateRole(false, privs.toString());
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 删除用户权限
     *
     * @param roleId Role名称
     * @return 删除结果
     * @throws IOException ex
     */
    public boolean deleteUserRole(String roleId) throws IOException {
        return pveClient.getAccess()
                .getRoles().get(roleId)
                .deleteRole().isSuccessStatusCode();
    }

    /**
     * 获取VM IpSet列表
     *
     * @param nodeName 节点
     * @param vmid     vmid
     * @return IpSet列表
     * @throws IOException ex
     */
    public List<IpSet> getVmIpSetList(String nodeName, int vmid) throws IOException {
        return objectMapper.readValue(
                pveClient.getNodes().get(nodeName)
                        .getQemu().get(vmid)
                        .getFirewall().getIpset()
                        .ipsetIndex().getResponse()
                        .getJSONArray("data").toString(),
                new TypeReference<List<IpSet>>() {
                }
        );
    }

    /**
     * 创建VM IpSet
     *
     * @param nodeName  节点
     * @param vmid      vmid
     * @param ipSetName IpSet名称
     * @param comment   备注
     * @return 成功？
     * @throws IOException ex
     */
    public boolean createVmIpSet(String nodeName, int vmid, String ipSetName, String comment) throws IOException {
        return pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getFirewall().getIpset()
                .createIpset(
                        ipSetName,
                        comment,
                        null,
                        null
                ).isSuccessStatusCode();
    }

    /**
     * 删除VM IpSet
     *
     * @param nodeName  节点
     * @param vmid      vmid
     * @param ipSetName IpSet名称
     * @return 成功？
     * @throws IOException ex
     */
    public boolean deleteVmIpSet(String nodeName, int vmid, String ipSetName) throws IOException {
        return pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getFirewall().getIpset()
                .get(ipSetName).deleteIpset().isSuccessStatusCode();
    }

    /**
     * 获取VM IpSet Ip/Cidr列表
     *
     * @param nodeName  节点
     * @param vmid      vmid
     * @param ipSetName IpSet名称
     * @return IpCidr列表
     * @throws IOException ex
     */
    public List<IpCidr> getVmIpCidrList(String nodeName, int vmid, String ipSetName) throws IOException {
        return objectMapper.readValue(
                pveClient.getNodes().get(nodeName)
                        .getQemu().get(vmid)
                        .getFirewall().getIpset()
                        .get(ipSetName).getIpset()
                        .getResponse().getJSONArray("data").toString(),
                new TypeReference<List<IpCidr>>() {
                }
        );
    }

    /**
     * 创建VM IpSet IpCidr
     *
     * @param nodeName  节点
     * @param vmid      vmid
     * @param ipSetName IpSet名称
     * @param cidr      ip地址或者网段CIDR
     * @param comment   备注
     * @param noMatch
     * @return 成功？
     * @throws IOException ex
     */
    public boolean createVmIpCidr(String nodeName, int vmid, String ipSetName, String cidr, String comment, boolean noMatch) throws IOException {
        return pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getFirewall().getIpset()
                .get(ipSetName).createIp(
                        cidr,
                        comment,
                        noMatch
                ).isSuccessStatusCode();
    }

    /**
     * 删除VM IpSet IpCidr
     *
     * @param nodeName  节点
     * @param vmid      vmid
     * @param ipSetName IpSet名称
     * @param cidr      ip地址或者网段CIDR
     * @return 成功？
     * @throws IOException ex
     */
    public boolean deleteVmIpCidr(String nodeName, int vmid, String ipSetName, String cidr) throws IOException {
        return pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getFirewall().getIpset()
                .get(ipSetName).get(cidr)
                .removeIp().isSuccessStatusCode();
    }


    //////////////////////////////////////////
    //                                      //
    //              功能方法                 //
    //                                      //
    //////////////////////////////////////////

    /*
        QemuGuestAgent相关操作
     */

    /**
     * 调用Qemu-Guest-Agent 修改客户机OS密码
     *
     * @param nodeName 节点name
     * @param vmid     目标VMID
     * @param username 用户名
     * @param password 密码
     * @return 结果
     */
    public boolean modifyVmOsPasswordByQemuGuestAgent(String nodeName, int vmid, String username, String password) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getAgent().getSetUserPassword()
                .setUserPassword(password, username);
        //如果响应体JSONObject包含data字段，则说明执行成功
        return pveResult.getResponse().has("data");
    }

    /**
     * 调用Qemu-Guest-Agent执行命令
     *
     * @param nodeName   节点name
     * @param vmid       目标VMID
     * @param cmd        命令行
     * @param input_data 命令行参数
     * @return 执行PID
     */
    public int executeCommandByQemuGuestAgent(String nodeName, int vmid, String cmd, String input_data) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getAgent().getExec()
                .exec(cmd, input_data);
        return pveResult.getResponse().getJSONObject("data").getInt("pid");
    }


    /**
     * 调用Qemu-Guest-Agent获取执行命令结果
     *
     * @param nodeName 节点name
     * @param vmid     目标VMID
     * @param pid      执行PID
     * @return 输出内容
     */
    public String getQemuGuestAgentCommandOutPut(String nodeName, int vmid, int pid) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getAgent().getExecStatus()
                .execStatus(pid);
        return pveResult.getResponse().getString("out-data");
    }

    /*
        Cloud-Init相关操作
     */

    /**
     * 设置CloudInit驱动器配置的密码
     *
     * @param nodeName 节点
     * @param vmid     VMID
     * @param password 密码
     * @return 成功
     */
    public boolean setVmCloudInitUserPassword(String nodeName, int vmid, String username, String password) throws IOException {
        String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8);
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(
                        null, null, null, null, null, null, null, null, null, null, null, null, null,
                        encodedPassword, null, username,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 设置VM CloudInit初始化IP
     *
     * @param nodeName       节点
     * @param vmid           VMID
     * @param netN           网卡序号
     * @param ipConfigTypeV4 ipv4配置类型 static dhcp
     * @param ipv4           ipv4地址 仅在ipv4配置类型为static时有效
     * @param netmaskBitV4   ipv4掩码长度 8-32 仅在ipv4配置类型为static时有效
     * @param gatewayV4      ipv4网关 仅在ipv4配置类型为static时有效
     * @param ipConfigTypeV6 ipv6配置类型 static dhcp slaac
     * @param ipv6           ipv6地址 仅在ipv6配置类型为static时有效
     * @param netmaskBitV6   ipv6掩码长度 8-128 仅在ipv6配置类型为static时有效
     * @param gatewayV6      ipv6网关 仅在ipv6配置类型为static时有效
     * @return 操作结果
     */
    public boolean setVmCloudInitIpConfig(
            String nodeName,
            int vmid,
            int netN,
            String ipConfigTypeV4,
            String ipv4,
            int netmaskBitV4,
            String gatewayV4,
            String ipConfigTypeV6,
            String ipv6,
            int netmaskBitV6,
            String gatewayV6
    ) throws IOException {
        // ip=10.1.0.198/24,gw=10.1.0.1,ip6=240e::2/64,gw6=240e::1
        // ip=10.1.0.198/24,gw=10.1.0.1,ip6=auto

        String ipv4configLine = "";
        String ipv6configLine = "";

        switch (ipConfigTypeV4) {
            case VMConfig.NetConfig.IpConfigTypeV4_DHCP -> ipv4configLine = "ip=dhcp";
            case VMConfig.NetConfig.IpConfigTypeV4_STATIC ->
                    ipv4configLine = "ip=" + ipv4 + "/" + netmaskBitV4 + ",gw=" + gatewayV4;
        }

        switch (ipConfigTypeV6) {
            case VMConfig.NetConfig.IpConfigTypeV6_SLAAC -> ipv6configLine = "ip6=auto";
            case VMConfig.NetConfig.IpConfigTypeV6_DHCP -> ipv6configLine = "ip6=dhcp";
            case VMConfig.NetConfig.IpConfigTypeV6_STATIC ->
                    ipv6configLine = "ip6=" + ipv6 + "/" + netmaskBitV6 + ",gw6=" + gatewayV6;
        }

        String configLine = URLEncoder.encode(ipv4configLine + "," + ipv6configLine, StandardCharsets.UTF_8);

        //构造map
        Map<Integer, String> ipconfigNMap = new HashMap<>();
        ipconfigNMap.put(netN, configLine);

        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        ipconfigNMap, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null
                );
        return pveResult.isSuccessStatusCode();
    }

    /*
        电源管理
     */

    /**
     * 启动VM
     *
     * @param vmid vmid
     * @return UPID
     */
    public String startVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getStart().vmStart();
        return pveResult.getResponse().getString("data");
    }

    /**
     * 软关机
     *
     * @param vmid vmid
     * @return UPID
     */
    public String shutdownVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getShutdown().vmShutdown();
        return pveResult.getResponse().getString("data");
    }

    /**
     * 关机再开机
     *
     * @param vmid
     * @return UPID
     */
    public String rebootVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getReboot().vmReboot();
        return pveResult.getResponse().getString("data");
    }

    /**
     * 关机再开机，关机超时设置时间
     *
     * @param vmid    vmid
     * @param timeout 超时
     * @return UPID
     */
    public String rebootVM(String nodeName, int vmid, int timeout) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getReboot().vmReboot(timeout);
        return pveResult.getResponse().getString("data");
    }

    /**
     * 挂起VM
     *
     * @param vmid vmid
     * @return UPID
     */
    public String suspendVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getSuspend().vmSuspend();
        return pveResult.getResponse().getString("data");
    }

    /**
     * 强制关闭VM
     *
     * @param vmid vmid
     * @return UPID
     */
    public String stopVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult =
                pveClient.getNodes().get(nodeName)
                        .getQemu().get(vmid).getStatus().getStop().vmStop();
        return pveResult.getResponse().getString("data");
    }

    /**
     * 强制重启
     *
     * @param vmid vmid
     * @return UPID
     */
    public String resetVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult =
                pveClient.getNodes().get(nodeName)
                        .getQemu().get(vmid).getStatus().getReset().vmReset();
        return pveResult.getResponse().getString("data");
    }

    /**
     * 删除一个虚拟机
     *
     * @param vmid vmid
     * @return UPID
     */
    public String deleteVm(String nodeName, int vmid) throws IOException {
        PveResult pveResult =
                pveClient.getNodes().get(nodeName)
                        .getQemu().get(vmid).destroyVm();
        return pveResult.getResponse().getString("data");
    }

    //////////////////////////////////////////
    //                                      //
    //              工具方法                 //
    //                                      //
    //////////////////////////////////////////


    /**
     * 获取当前节点已创建的VM分配的内存总量(所有VM)
     *
     * @param nodeName 节点
     * @return 内存分配总额
     * @throws IOException e
     */
    public double calculateAllVmAllocatedRamGbs(String nodeName) throws IOException {
        return this.calculateAllVmAllocatedRamGbs(
                nodeName,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE
        );
    }

    /**
     * 获取当前节点已创建的VM分配的内存总量(限制VMID范围)
     *
     * @param nodeName  节点
     * @param vmidBegin VMID起始
     * @param vmidEnd   VMID结束
     * @return 内存分配总额
     * @throws IOException e
     */
    public double calculateAllVmAllocatedRamGbs(String nodeName, int vmidBegin, int vmidEnd) throws IOException {
        //获取所有VM信息
        PveResult vmlistResult = pveClient.getNodes().get(nodeName)
                .getQemu().vmlist();
        List<VMInfo> allVmList = objectMapper.readValue(
                vmlistResult.getResponse().getJSONArray("data").toString(),
                new TypeReference<List<VMInfo>>() {
                }
        );
        //存储内存总量计数
        double ram_gbs_used = 0.0;
        //遍历
        for (VMInfo vmInfo : allVmList) {
            //处于VMID分配范围内的VM才计算
            if (vmInfo.getVmid() >= vmidBegin && vmInfo.getVmid() <= vmidEnd) {
                ram_gbs_used += vmInfo.getMaxmem() * Math.pow(1024, -3);
            }
        }
        return ram_gbs_used;
    }

    /**
     * 寻找一个可用的VMID
     *
     * @param nodeName  节点
     * @param vmidBegin 起始ID号
     * @param vmidEnd   结束ID号
     * @return 查找到的ID，没有符合条件的ID则返回NULL
     * @throws IOException e
     */
    public Integer findAvailableVmid(String nodeName, int vmidBegin, int vmidEnd) throws IOException {
        //获取所有VMID
        Set<Integer> vmidSet = this.getVmidSet(nodeName);
        Integer newVmid = null;
        for (int vmid = vmidBegin; vmid < vmidEnd; vmid++) {
            if (!vmidSet.contains(vmid)) {
                newVmid = vmid;
                break;
            }
        }
        return newVmid;
    }

    /**
     * 等待任务完成
     *
     * @param upid     任务id
     * @param waitStep 检查间隔 毫秒
     * @param timeOut  超时时间 毫秒
     */
    public boolean waitTaskFinish(String upid, int waitStep, int timeOut) throws IOException {
        return pveClient.waitForTaskToFinish(
                upid,
                waitStep,
                timeOut
        );
    }

}
