package net.dabaiyun.proxmoxsdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.corsinvest.proxmoxve.api.PveClient;
import it.corsinvest.proxmoxve.api.PveResult;
import net.dabaiyun.proxmoxsdk.entity.*;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.dabaiyun.proxmoxsdk.entity.VMConfig.DiskConfig.*;

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

    public void setDebugLevel(int level) {
        pveClient.setDebugLevel(level);
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
                new TypeReference<StorageInfo>() {
                }
        );
        storageInfo.setStorage(storageName);
        return storageInfo;
    }

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
                new TypeReference<List<TaskInfo>>() {
                }
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
                new TypeReference<TaskInfo>() {
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

    /**
     * 创建VM备份
     *
     * @param nodeName       节点
     * @param vmid           VMID
     * @param storage        存储区
     * @param mode           模式       snapshot | suspend | stop
     * @param compress       压缩方式    0 | 1 | gzip | lzo | zstd
     * @param remove         删除旧的
     * @param protected_     受保护的（不会被自动备份自动删除）
     * @param notes_template 描述文本模板
     * @return UPID
     * @throws IOException E
     */
    public String backUpVm(
            String nodeName,
            int vmid,
            String storage,
            String mode,
            String compress,
            Boolean remove,
            Boolean protected_,
            String notes_template
    ) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getVzdump().vzdump(null, null,
                        compress, null, null, null, null, null, null, null, null,
                        mode,
                        notes_template, null, null, null,
                        protected_, null, null, remove, null, null, null, null, null,
                        storage, null,
                        String.valueOf(vmid), null
                );
        return pveResult.getResponse().getString("data");
    }


    /**
     * 备份恢复VM
     *
     * @param nodeName 节点名称
     * @param vmid     VMID
     * @param storage  存储区
     * @param archive  备份文件的volid eg. HDD-Raid6:backup/vzdump-qemu-1007-2024_08_14-05_20_59.vma.zst
     * @return 恢复Job的UPID
     */
    public String restoreVm(String nodeName, int vmid, String storage, String archive) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().createVm(
                        vmid, null, null, null, null,
                        archive, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        true, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        storage, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null
                );
        return pveResult.getResponse().getString("data");
    }

    /**
     * 创建VM
     *
     * @param vmid               The (unique) ID of the VM.
     * @param acpi               Enable/disable ACPI.
     * @param affinity           List of host cores used to execute guest
     *                           processes, for example: 0,5,8-11
     * @param agent              Enable/disable communication with the QEMU Guest
     *                           Agent and its properties.
     * @param arch               Virtual processor architecture. Defaults to the
     *                           host. Enum: x86_64,aarch64
     * @param archive            The backup archive. Either the file system
     *                           path to a .tar or .vma file (use '-' to pipe data from stdin)
     *                           or a proxmox storage backup volume identifier.
     * @param args               Arbitrary arguments passed to kvm.
     * @param audio0             Configure a audio device, useful in combination
     *                           with QXL/Spice.
     * @param autostart          Automatic restart after crash (currently
     *                           ignored).
     * @param balloon            Amount of target RAM for the VM in MiB. Using
     *                           zero disables the ballon driver.
     * @param bios               Select BIOS implementation. Enum: seabios,ovmf
     * @param boot               Specify guest boot order. Use the 'order='
     *                           sub-property as usage with no key or 'legacy=' is deprecated.
     * @param bootdisk           Enable booting from specified disk.
     *                           Deprecated: Use 'boot: order=foo;bar' instead.
     * @param bwlimit            Override I/O bandwidth limit (in KiB/s).
     * @param cdrom              This is an alias for option -ide2
     * @param cicustom           cloud-init: Specify custom files to replace
     *                           the automatically generated ones at start.
     * @param cipassword         cloud-init: Password to assign the user.
     *                           Using this is generally not recommended. Use ssh keys
     *                           instead. Also note that older cloud-init versions do not
     *                           support hashed passwords.
     * @param citype             Specifies the cloud-init configuration format.
     *                           The default depends on the configured operating system type
     *                           (`ostype`. We use the `nocloud` format for Linux, and
     *                           `configdrive2` for windows. Enum:
     *                           configdrive2,nocloud,opennebula
     * @param ciuser             cloud-init: User name to change ssh keys and
     *                           password for instead of the image's configured default user.
     * @param cores              The number of cores per socket.
     * @param cpu                Emulated CPU type.
     * @param cpulimit           Limit of CPU usage.
     * @param cpuunits           CPU weight for a VM, will be clamped to [1,
     *                           10000] in cgroup v2.
     * @param description        Description for the VM. Shown in the
     *                           web-interface VM's summary. This is saved as comment inside
     *                           the configuration file.
     * @param efidisk0           Configure a disk for storing EFI vars. Use
     *                           the special syntax STORAGE_ID:SIZE_IN_GiB to allocate a new
     *                           volume. Note that SIZE_IN_GiB is ignored here and that the
     *                           default EFI vars are copied to the volume instead. Use
     *                           STORAGE_ID:0 and the 'import-from' parameter to import from
     *                           an existing volume.
     * @param force              Allow to overwrite existing VM.
     * @param freeze             Freeze CPU at startup (use 'c' monitor command
     *                           to start execution).
     * @param hookscript         Script that will be executed during various
     *                           steps in the vms lifetime.
     * @param hostpciN           Map host PCI devices into guest.
     * @param hotplug            Selectively enable hotplug features. This is a
     *                           comma separated list of hotplug features: 'network', 'disk',
     *                           'cpu', 'memory', 'usb' and 'cloudinit'. Use '0' to disable
     *                           hotplug completely. Using '1' as value is an alias for the
     *                           default `network,disk,usb`. USB hotplugging is possible for
     *                           guests with machine version &amp;gt;= 7.1 and ostype l26 or
     *                           windows &amp;gt; 7.
     * @param hugepages          Enable/disable hugepages memory. Enum:
     *                           any,2,1024
     * @param ideN               Use volume as IDE hard disk or CD-ROM (n is 0 to
     *                           3). Use the special syntax STORAGE_ID:SIZE_IN_GiB to allocate
     *                           a new volume. Use STORAGE_ID:0 and the 'import-from'
     *                           parameter to import from an existing volume.
     * @param ipconfigN          cloud-init: Specify IP addresses and
     *                           gateways for the corresponding interface. IP addresses use
     *                           CIDR notation, gateways are optional but need an IP of the
     *                           same type specified. The special string 'dhcp' can be used
     *                           for IP addresses to use DHCP, in which case no explicit
     *                           gateway should be provided. For IPv6 the special string
     *                           'auto' can be used to use stateless autoconfiguration. This
     *                           requires cloud-init 19.4 or newer. If cloud-init is enabled
     *                           and neither an IPv4 nor an IPv6 address is specified, it
     *                           defaults to using dhcp on IPv4.
     * @param ivshmem            Inter-VM shared memory. Useful for direct
     *                           communication between VMs, or to the host.
     * @param keephugepages      Use together with hugepages. If enabled,
     *                           hugepages will not not be deleted after VM shutdown and can
     *                           be used for subsequent starts.
     * @param keyboard           Keyboard layout for VNC server. This option
     *                           is generally not required and is often better handled from
     *                           within the guest OS. Enum:
     *                           de,de-ch,da,en-gb,en-us,es,fi,fr,fr-be,fr-ca,fr-ch,hu,is,it,ja,lt,mk,nl,no,pl,pt,pt-br,sv,sl,tr
     * @param kvm                Enable/disable KVM hardware virtualization.
     * @param live_restore       Start the VM immediately from the backup
     *                           and restore in background. PBS only.
     * @param localtime          Set the real time clock (RTC) to local time.
     *                           This is enabled by default if the `ostype` indicates a
     *                           Microsoft Windows OS.
     * @param lock_              Lock/unlock the VM. Enum:
     *                           backup,clone,create,migrate,rollback,snapshot,snapshot-delete,suspending,suspended
     * @param machine            Specifies the QEMU machine type.
     * @param memory             Amount of RAM for the VM in MiB. This is the
     *                           maximum available memory when you use the balloon device.
     * @param migrate_downtime   Set maximum tolerated downtime (in
     *                           seconds) for migrations.
     * @param migrate_speed      Set maximum speed (in MB/s) for
     *                           migrations. Value 0 is no limit.
     * @param name               Set a name for the VM. Only used on the
     *                           configuration web interface.
     * @param nameserver         cloud-init: Sets DNS server IP address for
     *                           a container. Create will automatically use the setting from
     *                           the host if neither searchdomain nor nameserver are set.
     * @param netN               Specify network devices.
     * @param numa               Enable/disable NUMA.
     * @param numaN              NUMA topology.
     * @param onboot             Specifies whether a VM will be started during
     *                           system bootup.
     * @param ostype             Specify guest operating system. Enum:
     *                           other,wxp,w2k,w2k3,w2k8,wvista,win7,win8,win10,win11,l24,l26,solaris
     * @param parallelN          Map host parallel devices (n is 0 to 2).
     * @param pool               Add the VM to the specified pool.
     * @param protection         Sets the protection flag of the VM. This
     *                           will disable the remove VM and remove disk operations.
     * @param reboot             Allow reboot. If set to '0' the VM exit on
     *                           reboot.
     * @param rng0               Configure a VirtIO-based Random Number Generator.
     * @param sataN              Use volume as SATA hard disk or CD-ROM (n is 0
     *                           to 5). Use the special syntax STORAGE_ID:SIZE_IN_GiB to
     *                           allocate a new volume. Use STORAGE_ID:0 and the 'import-from'
     *                           parameter to import from an existing volume.
     * @param scsiN              Use volume as SCSI hard disk or CD-ROM (n is 0
     *                           to 30). Use the special syntax STORAGE_ID:SIZE_IN_GiB to
     *                           allocate a new volume. Use STORAGE_ID:0 and the 'import-from'
     *                           parameter to import from an existing volume.
     * @param scsihw             SCSI controller model Enum:
     *                           lsi,lsi53c810,virtio-scsi-pci,virtio-scsi-single,megasas,pvscsi
     * @param searchdomain       cloud-init: Sets DNS search domains for a
     *                           container. Create will automatically use the setting from the
     *                           host if neither searchdomain nor nameserver are set.
     * @param serialN            Create a serial device inside the VM (n is 0
     *                           to 3)
     * @param shares             Amount of memory shares for auto-ballooning.
     *                           The larger the number is, the more memory this VM gets.
     *                           Number is relative to weights of all other running VMs. Using
     *                           zero disables auto-ballooning. Auto-ballooning is done by
     *                           pvestatd.
     * @param smbios1            Specify SMBIOS type 1 fields.
     * @param smp                The number of CPUs. Please use option -sockets
     *                           instead.
     * @param sockets            The number of CPU sockets.
     * @param spice_enhancements Configure additional enhancements
     *                           for SPICE.
     * @param sshkeys            cloud-init: Setup public SSH keys (one key per
     *                           line, OpenSSH format).
     * @param start              Start VM after it was created successfully.
     * @param startdate          Set the initial date of the real time clock.
     *                           Valid format for date are:'now' or '2006-06-17T16:01:21' or
     *                           '2006-06-17'.
     * @param startup            Startup and shutdown behavior. Order is a
     *                           non-negative number defining the general startup order.
     *                           Shutdown in done with reverse ordering. Additionally you can
     *                           set the 'up' or 'down' delay in seconds, which specifies a
     *                           delay to wait before the next VM is started or stopped.
     * @param storage            Default storage.
     * @param tablet             Enable/disable the USB tablet device.
     * @param tags               Tags of the VM. This is only meta information.
     * @param tdf                Enable/disable time drift fix.
     * @param template           Enable/disable Template.
     * @param tpmstate0          Configure a Disk for storing TPM state. The
     *                           format is fixed to 'raw'. Use the special syntax
     *                           STORAGE_ID:SIZE_IN_GiB to allocate a new volume. Note that
     *                           SIZE_IN_GiB is ignored here and 4 MiB will be used instead.
     *                           Use STORAGE_ID:0 and the 'import-from' parameter to import
     *                           from an existing volume.
     * @param unique             Assign a unique random ethernet address.
     * @param unusedN            Reference to unused volumes. This is used
     *                           internally, and should not be modified manually.
     * @param usbN               Configure an USB device (n is 0 to 4, for machine
     *                           version &amp;gt;= 7.1 and ostype l26 or windows &amp;gt; 7, n
     *                           can be up to 14).
     * @param vcpus              Number of hotplugged vcpus.
     * @param vga                Configure the VGA hardware.
     * @param virtioN            Use volume as VIRTIO hard disk (n is 0 to 15).
     *                           Use the special syntax STORAGE_ID:SIZE_IN_GiB to allocate a
     *                           new volume. Use STORAGE_ID:0 and the 'import-from' parameter
     *                           to import from an existing volume.
     * @param vmgenid            Set VM Generation ID. Use '1' to autogenerate
     *                           on create or update, pass '0' to disable explicitly.
     * @param vmstatestorage     Default storage for VM state
     *                           volumes/files.
     * @param watchdog           Create a virtual hardware watchdog device.
     * @return 创建Job的UPID
     */
    public String createVmStandard(String nodeName, int vmid, Boolean acpi, String affinity, String agent, String arch, String archive, String args, String audio0, Boolean autostart, Integer balloon, String bios, String boot, String bootdisk, Integer bwlimit, String cdrom, String cicustom, String cipassword, String citype, String ciuser, Integer cores, String cpu, Float cpulimit, Integer cpuunits, String description, String efidisk0, Boolean force, Boolean freeze, String hookscript, Map<Integer, String> hostpciN, String hotplug, String hugepages, Map<Integer, String> ideN, Map<Integer, String> ipconfigN, String ivshmem, Boolean keephugepages, String keyboard, Boolean kvm, Boolean live_restore, Boolean localtime, String lock_, String machine, Integer memory, Float migrate_downtime, Integer migrate_speed, String name, String nameserver, Map<Integer, String> netN, Boolean numa, Map<Integer, String> numaN, Boolean onboot, String ostype, Map<Integer, String> parallelN, String pool, Boolean protection, Boolean reboot, String rng0, Map<Integer, String> sataN, Map<Integer, String> scsiN, String scsihw, String searchdomain, Map<Integer, String> serialN, Integer shares, String smbios1, Integer smp, Integer sockets, String spice_enhancements, String sshkeys, Boolean start, String startdate, String startup, String storage, Boolean tablet, String tags, Boolean tdf, Boolean template, String tpmstate0, Boolean unique, Map<Integer, String> unusedN, Map<Integer, String> usbN, Integer vcpus, String vga, Map<Integer, String> virtioN, String vmgenid, String vmstatestorage, String watchdog) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().createVm(vmid, acpi, affinity, agent, arch, archive, args, audio0, autostart, balloon, bios, boot, bootdisk, bwlimit, cdrom, cicustom, cipassword, citype, ciuser, cores, cpu, cpulimit, cpuunits, description, efidisk0, force, freeze, hookscript, hostpciN, hotplug, hugepages, ideN, ipconfigN, ivshmem, keephugepages, keyboard, kvm, live_restore, localtime, lock_, machine, memory, migrate_downtime, migrate_speed, name, nameserver, netN, numa, numaN, onboot, ostype, parallelN, pool, protection, reboot, rng0, sataN, scsiN, scsihw, searchdomain, serialN, shares, smbios1, smp, sockets, spice_enhancements, sshkeys, start, startdate, startup, storage, tablet, tags, tdf, template, tpmstate0, unique, unusedN, usbN, vcpus, vga, virtioN, vmgenid, vmstatestorage, watchdog);
        return pveResult.getResponse().getString("data");
    }

    /**
     * 创建标准虚拟机
     *
     * @param nodeName    节点名称
     * @param storage     存储区
     * @param vmid        VMID
     * @param vmName      VM名称
     * @param osType      OS类型
     *                    //     * @param diskType              磁盘类型
     * @param diskSizeGb  磁盘大小
     * @param cpuCores    CPU核心数
     * @param maxMemoryMb 最大内存
     * @param minMemoryMb 最小内存
     *                    //     * @param netType               网卡类型
     * @param netBridge   网桥
//     * @param machineType 设备类型
     * @param description 描述
     * @return UPID
     * @throws IOException e
     */
    public String createVmStandard(
            String nodeName,
            String storage,
            int vmid,
            String vmName,
            String osType,
//            String diskType,
            int diskSizeGb,
            int cpuCores,
            int maxMemoryMb,
            int minMemoryMb,
//            String netType,
            String netBridge,
//            String machineType,
            String description
    ) throws IOException {
        Integer memory = Math.max(maxMemoryMb, minMemoryMb);
        Integer balloon = null;
        if (maxMemoryMb != minMemoryMb) {
            balloon = Math.min(maxMemoryMb, minMemoryMb);
        }
        //Disk
//        Map<Integer, String> ideN = null;
//        Map<Integer, String> sataN = null;
//        Map<Integer, String> scsiN = null;
        Map<Integer, String> virtioN = null;
        virtioN = new HashMap<>();
        String DiskonfigLine = storage + ":" + diskSizeGb + ",format=qcow2"
                + ",iothread=on";
        virtioN.put(0, URLEncoder.encode(DiskonfigLine, StandardCharsets.UTF_8));
//        switch (diskType) {
//            case DeviceType_IDE -> {
//                ideN = new HashMap<>();
//                String configLine = storage + ":" + diskSizeGb + ",format=qcow2";
//                ideN.put(0, URLEncoder.encode(configLine, StandardCharsets.UTF_8));
//            }
//            case DeviceType_SATA -> {
//                sataN = new HashMap<>();
//                String configLine = storage + ":" + diskSizeGb + ",format=qcow2";
//                sataN.put(0, URLEncoder.encode(configLine, StandardCharsets.UTF_8));
//            }
//            case DeviceType_SCSI -> {
//                scsiN = new HashMap<>();
//                String configLine = storage + ":" + diskSizeGb + ",format=qcow2"
//                        + ",iothread=on";
//                scsiN.put(0, URLEncoder.encode(configLine, StandardCharsets.UTF_8));
//            }
//            case DeviceType_VirtIO -> {
//                virtioN = new HashMap<>();
//                String configLine = storage + ":" + diskSizeGb + ",format=qcow2"
//                        + ",iothread=on";
//                virtioN.put(0, URLEncoder.encode(configLine, StandardCharsets.UTF_8));
//            }
//            default -> {
//                //未知磁盘设备类型
//                System.out.println(this.getClass().getSimpleName() + ": Unknown disk type: " + diskType);
//            }
//        }

        //Net
        Map<Integer, String> netN = new HashMap<>();
        String netConfigLine = VMConfig.NetConfig.DeviceType_VirtIO + ",bridge=" + netBridge;
        netN.put(0, URLEncoder.encode(netConfigLine, StandardCharsets.UTF_8));

        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().createVm(
                        vmid, null, null, null, null, null, null, null, null,
                        balloon, null, null, null, null, null, null, null, null, null,
                        cpuCores,
                        "host", null, null,
                        description, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null,
                        VMConfig.MachineType_q35,
                        memory, null, null,
                        vmName, null,
                        netN,
                        false, null, null,
                        osType, null, null, null, null, null,
                        null,
                        null,
                        VMConfig.SCSI_HW_VirtIO_SCSI_Single,
                        null, null, null, null, null,
                        1, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null,
                        virtioN, null,
                        null, null
                );
        return pveResult.getResponse().getString("data");
    }

    /**
     * 创建标准虚拟机
     *
     * @param nodeName    节点名称
     * @param storage     存储区
     * @param vmid        VMID
     * @param vmName      VM名称
     * @param osType      OS类型
     *                    //     * @param diskType              磁盘类型
     * @param diskSizeGb  磁盘大小
     * @param cpuCores    CPU核心数
     * @param maxMemoryMb 最大内存
     * @param minMemoryMb 最小内存
     *                    //     * @param netType               网卡类型
     * @param netBridge   网桥
//     * @param machineType 设备类型
     * @param description 描述
     * @return UPID
     * @throws IOException e
     */
    public String createVmCompatible(
            String nodeName,
            String storage,
            int vmid,
            String vmName,
            String osType,
//            String diskType,
            int diskSizeGb,
            int cpuCores,
            int maxMemoryMb,
            int minMemoryMb,
//            String netType,
            String netBridge,
//            String machineType,
            String description
    ) throws IOException {
        Integer memory = Math.max(maxMemoryMb, minMemoryMb);
        Integer balloon = null;
        if (maxMemoryMb != minMemoryMb) {
            balloon = Math.min(maxMemoryMb, minMemoryMb);
        }
        //Disk
//        Map<Integer, String> ideN = null;
        Map<Integer, String> sataN = null;
        sataN = new HashMap<>();
        String diskConfigLine = storage + ":" + diskSizeGb + ",format=qcow2";
        sataN.put(0, URLEncoder.encode(diskConfigLine, StandardCharsets.UTF_8));
//        Map<Integer, String> scsiN = null;
//        Map<Integer, String> virtioN = null;
//        switch (diskType) {
//            case DeviceType_IDE -> {
//                ideN = new HashMap<>();
//                String configLine = storage + ":" + diskSizeGb + ",format=qcow2";
//                ideN.put(0, URLEncoder.encode(configLine, StandardCharsets.UTF_8));
//            }
//            case DeviceType_SATA -> {
//                sataN = new HashMap<>();
//                String configLine = storage + ":" + diskSizeGb + ",format=qcow2";
//                sataN.put(0, URLEncoder.encode(configLine, StandardCharsets.UTF_8));
//            }
//            case DeviceType_SCSI -> {
//                scsiN = new HashMap<>();
//                String configLine = storage + ":" + diskSizeGb + ",format=qcow2"
//                        + ",iothread=on";
//                scsiN.put(0, URLEncoder.encode(configLine, StandardCharsets.UTF_8));
//            }
//            case DeviceType_VirtIO -> {
//                virtioN = new HashMap<>();
//                String configLine = storage + ":" + diskSizeGb + ",format=qcow2"
//                        + ",iothread=on";
//                virtioN.put(0, URLEncoder.encode(configLine, StandardCharsets.UTF_8));
//            }
//            default -> {
//                //未知磁盘设备类型
//                System.out.println(this.getClass().getSimpleName() + ": Unknown disk type: " + diskType);
//            }
//        }

        //Net
        Map<Integer, String> netN = new HashMap<>();
        String netConfigLine = VMConfig.NetConfig.DeviceType_E1000 + ",bridge=" + netBridge;
        netN.put(0, URLEncoder.encode(netConfigLine, StandardCharsets.UTF_8));

        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getQemu().createVm(
                        vmid, null, null, null, null, null, null, null, null,
                        balloon, null, null, null, null, null, null, null, null, null,
                        cpuCores,
                        "host", null, null,
                        description, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null,
                        VMConfig.MachineType_i440fx,
                        memory, null, null,
                        vmName, null,
                        netN,
                        false, null, null,
                        osType, null, null, null, null, null,
                        sataN,
                        null,
                        VMConfig.SCSI_HW_LSI53C895A,
                        null, null, null, null, null,
                        1, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null, null, null,
                        null, null,
                        null, null
                );
        return pveResult.getResponse().getString("data");
    }


    //////////////////////////////////////////
    //                                      //
    //            数据操作方法                //
    //                                      //
    //////////////////////////////////////////


    /**
     * 删除备份
     *
     * @param nodeName 节点名称
     * @param storage  存储区
     * @param volid    备份文件名
     * @return 操作结果
     * @throws IOException e
     */
    public Boolean deleteBackup(String nodeName, String storage, String volid) throws IOException {
        PveResult pveResult = pveClient.getNodes().get(nodeName)
                .getStorage().get(storage)
                .getContent().get(StorageContent.ContentType_BACKUP)
                .delete(volid);
        return pveResult.isSuccessStatusCode();
    }

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
        diskDeviceTypeList.add(DeviceType_IDE);
        diskDeviceTypeList.add(DeviceType_SATA);
        diskDeviceTypeList.add(DeviceType_SCSI);
        diskDeviceTypeList.add(DeviceType_VirtIO);
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

        //hostpci
        Set<String> pciDeviceSet = new HashSet<>();

        for (String key : dataJsonObject.keySet()) {
            if (key.startsWith("hostpci")) {
                pciDeviceSet.add(key);
            }
        }

        for (String pciDevice : pciDeviceSet) {
            VMConfig.PciDeviceConfig pciDeviceConfig = new VMConfig.PciDeviceConfig();
            String configLine = dataJsonObject.getString(pciDevice);

            String[] configList = configLine.split(",");
            Integer deviceNumber = Integer.parseInt(pciDevice.replaceAll("\\D", ""));
            String pciBus = configList[0];
            String mdev = null;
            Boolean pcie = false;
            Boolean rombar = true;

            for (String string : configList) {
                String[] keyAndValue = string.split("=");
                String key = keyAndValue[0];

                switch (key) {
                    case "mdev" -> mdev = keyAndValue[1];
                    case "pcie" -> pcie = keyAndValue[1].equals("1");
                    case "rombar" -> rombar = keyAndValue[1].equals("1");
                    default -> {
                    }
                }
            }

            pciDeviceConfig.setPciBus(pciBus);
            pciDeviceConfig.setMdev(mdev);
            pciDeviceConfig.setPcie(pcie);
            pciDeviceConfig.setRombar(rombar);

            vmConfig.getPciDeviceMap().put(deviceNumber, pciDeviceConfig);
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
            case DeviceType_IDE -> {
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
            case DeviceType_SATA -> {
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
            case DeviceType_SCSI -> {
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
            case DeviceType_VirtIO -> {
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

    /**
     * 获取节点PCI设备列表
     *
     * @param nodeName 节点名称
     * @return PCI设备列表
     * @throws IOException e
     */
    public List<PciDevice> getPciDeviceList(String nodeName) throws IOException {
        return objectMapper.readValue(
                pveClient.getNodes().get(nodeName)
                        .getHardware().getPci().pciscan()
                        .getResponse().getJSONArray("data").toString(),
                new TypeReference<List<PciDevice>>() {
                }
        );
    }

    /**
     * 获取节点MDEV设备列表
     *
     * @param nodeName 节点名称
     * @param pciBus   宿主pci设备总线
     * @return MDEV设备清单
     * @throws IOException e
     */
    public List<PciMediatedDevice> getPciMdeviceList(String nodeName, String pciBus) throws IOException {
        return objectMapper.readValue(
                pveClient.getNodes().get(nodeName)
                        .getHardware().getPci().get(pciBus)
                        .getMdev().mdevscan()
                        .getResponse().getJSONArray("data").toString(),
                new TypeReference<List<PciMediatedDevice>>() {
                }
        );
    }

    /**
     * 添加PCI设备
     *
     * @param nodeName     节点名称
     * @param vmid         VMID
     * @param deviceNumber 设备序号，如0代表hostpci0，1代表hostpci1
     * @param pciBus       设备总线地址
     * @param mdev         虚拟设备（null为不启用mdev，直接直通整个pci设备）
     * @param pcie         是否pcie
     * @param rombar       是否rombar
     * @return 是否成功
     */
    public boolean setVmPcieDevice(
            String nodeName,
            Integer vmid,
            Integer deviceNumber,
            String pciBus,
            String mdev,
            Boolean pcie,
            Boolean rombar,
            Boolean allFunc
    ) throws IOException {
        //配置信息
        VMConfig vmConfig = this.getVmConfig(nodeName, vmid);
        //组装pcie配置项
        StringBuilder stringBuilder = new StringBuilder()
                .append((allFunc && mdev == null) ? pciBus.split("\\.")[0] : pciBus);
        if (mdev != null) {
            stringBuilder.append(",").append("mdev=" + mdev);
        }
        if (pcie) {
            stringBuilder.append(",").append("pcie=1");
        }
        if (!rombar) {
            stringBuilder.append(",").append("rombar=0");
        }
        String configLine = stringBuilder.toString();
        //构建hostpci map
        Map<Integer, String> hostpciN = new HashMap<>();
        hostpciN.put(deviceNumber, URLEncoder.encode(configLine, StandardCharsets.UTF_8));
        //添加设备
        PveResult pveResult = pveClient
                .getNodes().get(nodeName)
                .getQemu().get(vmid)
                .getConfig().updateVm(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                        vmConfig.getDigest(), null, null, null, null,
                        hostpciN, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
                );
        return pveResult.isSuccessStatusCode();
    }

    /**
     * 删除pci设备
     *
     * @param nodeName     节点名
     * @param vmid         VMID
     * @param deviceNumber 设备序号
     * @return 删除结果
     * @throws IOException e
     */
    public boolean deletePciDevice(String nodeName, int vmid, int deviceNumber) throws IOException {
        return this.deleteHardware(nodeName, vmid, "hostpci" + deviceNumber);
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
