package net.dabaiyun.proxmoxsdk.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRole {

    int special;
    String roleid;
    String privs;

    public List<String> getRoleStringList(){
        return List.of(this.privs.split(","));
    }

    public enum Group {
        Allocate("Group.Allocate");

        String val;

        Group(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public enum User {
        Modify("User.Modify");

        String val;

        User(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public enum Datastore {
        Audit("Datastore.Audit"),
        AllocateTemplate("Datastore.AllocateTemplate"),
        AllocateSpace("Datastore.AllocateSpace"),
        Allocate("Datastore.Allocate");

        String val;

        Datastore(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public enum VM {
        Audit("VM.Audit"),
        Monitor("VM.Monitor"),
        Migrate("VM.Migrate"),
        Backup("VM.Backup"),
        Console("VM.Console"),
        PowerMgmt("VM.PowerMgmt"),
        Allocate("VM.Allocate"),
        Clone("VM.Clone");

        String val;

        VM(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }

        public enum Snapshot {
            Snapshot("VM.Snapshot"),
            Rollback("VM.Snapshot.Rollback");

            String val;

            Snapshot(String val) {
                this.val = val;
            }

            @Override
            public String toString() {
                return val;
            }
        }

        public enum Config {
            Cloudinit("VM.Config.Cloudinit"),
            Options("VM.Config.Options"),
            Memory("VM.Config.Memory"),
            HWType("VM.Config.HWType"),
            Network("VM.Config.Network"),
            CPU("VM.Config.CPU"),
            Disk("VM.Config.Disk"),
            CDROM("VM.Config.CDROM");

            String val;

            Config(String val) {
                this.val = val;
            }

            @Override
            public String toString() {
                return val;
            }
        }
    }

    public enum Permissions {
        Modify("Permissions.Modify");

        String val;

        Permissions(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public enum Pool {
        Audit("Pool.Audit"),
        Allocate("Pool.Allocate");

        String val;

        Pool(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public enum SDN {
        Audit("SDN.Audit"),
        Use("SDN.Use"),
        Allocate("SDN.Allocate");

        String val;

        SDN(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public enum Sys {
        Audit("Sys.Audit"),
        Incoming("Sys.Incoming"),
        Syslog("Sys.Syslog"),
        Console("Sys.Console"),
        PowerMgmt("Sys.PowerMgmt"),
        Modify("Sys.Modify");

        String val;

        Sys(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    public enum Realm {
        AllocateUser("Realm.AllocateUser"),
        Allocate("Realm.Allocate");

        String val;

        Realm(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

}
