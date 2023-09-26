package net.dabaiyun.proxmoxsdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.corsinvest.proxmoxve.api.PveClient;
import it.corsinvest.proxmoxve.api.PveResult;
import net.dabaiyun.proxmoxsdk.entity.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return objectMapper.readValue(
                pveResult.getResponse().getJSONObject("data").toString(),
                new TypeReference<VMConfig>() {
                }
        );
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
     * 更换VM 虚拟光驱
     *
     * @param nodeName 节点
     * @param vmid     VMID
     * @return 成功？
     * @throws IOException ex
     */
    public boolean setVmCDROM(String nodeName, int vmid, String filePath) throws IOException {
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
     * 卸载VM虚拟光驱
     *
     * @param nodeName 节点
     * @param vmid     VMID
     * @return success
     */
    public boolean unlinkVMCDROM(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getUnlink().unlink("ide2");
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
     * 获取用户权限列表
     *
     * @return 权限对象列表
     * @throws IOException ex
     */
    public List<UserRole> getUserRoleList() throws IOException {
        PveResult pveResult = pveClient.getAccess().getRoles().index();
        return objectMapper.readValue(
                pveResult.getResponse().getJSONArray("data").toString(),
                new TypeReference<List<UserRole>>() {}
        );
    }

    /**
     * 创建UserRole
     * @param roleId Role名称
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
     * @param roleId Role名称
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
     * @param roleId Role名称
     * @return 删除结果
     * @throws IOException ex
     */
    public boolean deleteUserRole(String roleId) throws IOException {
        return pveClient.getAccess()
                .getRoles().get(roleId)
                .deleteRole().isSuccessStatusCode();
    }

//    /**
//     * 获取角色
//     * @param roleId Role名称
//     * @return UserRole
//     * @throws IOException ex
//     */
//    public UserRole getUserRole(String roleId) throws IOException {
//        PveResult pveResult = pveClient.getAccess().getRoles()
//                .get(roleId).readRole();
//        return objectMapper.readValue(
//                pveResult.getResponse().get("data").toString(),
//                new TypeReference<UserRole>() {}
//        );
//    }

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
    public boolean modifyCloudInitPassword(String nodeName, int vmid, String password) throws IOException {
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(
                        null, null, null, null, null, null, null, null, null, null, null, null, null,
                        password, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );
        return true;
    }

    /*
        电源管理
     */

    /**
     * 启动VM
     *
     * @param vmid vmid
     * @return success?
     */
    public boolean startVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getStart().vmStart();
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 软关机
     *
     * @param vmid vmid
     * @return success?
     */
    public boolean shutdownVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getShutdown().vmShutdown();
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 关机再开机
     *
     * @param vmid
     * @return success?
     */
    public boolean rebootVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getReboot().vmReboot();
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 关机再开机，关机超时设置时间
     *
     * @param vmid    vmid
     * @param timeout 超时
     * @return success?
     */
    public boolean rebootVM(String nodeName, int vmid, int timeout) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getReboot().vmReboot(timeout);
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 挂起VM
     *
     * @param vmid vmid
     * @return success?
     */
    public boolean suspendVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getStatus().getSuspend().vmSuspend();
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 强制关闭VM
     *
     * @param vmid vmid
     * @return success?
     */
    public boolean stopVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult =
                pveClient.getNodes().get(nodeName)
                        .getQemu().get(vmid).getStatus().getStop().vmStop();
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 强制重启
     *
     * @param vmid vmid
     * @return success?
     */
    public boolean resetVM(String nodeName, int vmid) throws IOException {
        PveResult pveResult =
                pveClient.getNodes().get(nodeName)
                        .getQemu().get(vmid).getStatus().getReset().vmReset();
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 删除一个虚拟机
     *
     * @param vmid vmid
     * @return success?
     */
    public boolean deleteVm(String nodeName, int vmid) throws IOException {
        PveResult pveResult =
                pveClient.getNodes().get(nodeName)
                        .getQemu().get(vmid).destroyVm();
        return true;
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
     * @param waitStep 检查间隔
     * @param timeOut  超时时间
     */
    public boolean waitTaskFinish(String upid, int waitStep, int timeOut) throws IOException {
        return pveClient.waitForTaskToFinish(
                upid,
                waitStep,
                timeOut //秒
        );
    }

}
