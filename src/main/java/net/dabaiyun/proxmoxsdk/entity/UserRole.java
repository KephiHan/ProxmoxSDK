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

        final String val;

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

        final String val;

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

        final String val;

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

        final String val;

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

            final String val;

            Snapshot(String val) {
                this.val = val;
            }

            @Override
            public String toString() {
                return val;
            }
        }

        public enum Config {
            CloudInit("VM.Config.Cloudinit"),
            Options("VM.Config.Options"),
            Memory("VM.Config.Memory"),
            HWType("VM.Config.HWType"),
            Network("VM.Config.Network"),
            CPU("VM.Config.CPU"),
            Disk("VM.Config.Disk"),
            CDROM("VM.Config.CDROM");

            final String val;

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

        final String val;

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

        final String val;

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

        final String val;

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

        final String val;

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

        final String val;

        Realm(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

}
