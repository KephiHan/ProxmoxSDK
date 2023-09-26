import net.dabaiyun.proxmoxsdk.ProxmoxClient;
import net.dabaiyun.proxmoxsdk.entity.RrdData;
import net.dabaiyun.proxmoxsdk.entity.UserRole;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.*;

public class JunitTest {
    
    private ProxmoxClient proxmoxClient;

    public JunitTest() throws IOException {
        proxmoxClient = new ProxmoxClient(
                "10.0.16.8",
                8006,
                "root",
                "password"
        );
    }

    @Test
    public void rrdDataTest() throws IOException {
        List<RrdData> rrdDataList = proxmoxClient.getVmHistoryPerformanceData("pve-e5", 1003);
        rrdDataList.forEach(System.out::println);
    }

    @Test
    public void getVersionTest() throws IOException {
        ProxmoxClient proxmoxClient = new ProxmoxClient(
                "10.0.16.8",
                8006,
                "root",
                "password1"
        );
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
    public void checkAuthValid(){
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
            if(userRole.getPrivs().equals("")){
                continue;
            }
            String[] roleNames = userRole.getPrivs().split(",");
            for (String roleName : roleNames) {
                String[] strings = roleName.split("\\.");

                if(strings.length == 2){
                    String group = strings[0];
                    String role = strings[1];
                    if(roleMap.containsKey(group)){
                        roleMap.get(group).put(role, null);
                    }else{
                        Map<String, Set<String>> secondMap = new HashMap<>();
                        secondMap.put(role, null);
                        roleMap.put(group, secondMap);
                    }
                }else if (strings.length == 3){
                    String group = strings[0];
                    String role = strings[1];
                    String subRole = strings[2];
                    if (roleMap.containsKey(group)) {
                        if(roleMap.get(group).containsKey(role) && roleMap.get(group).get(role) != null){
                            roleMap.get(group).get(role).add(subRole);
                        }else{
                            Set<String> subRoleSet = new HashSet<>();
                            subRoleSet.add(subRole);
                            roleMap.get(group).put(role, subRoleSet);
                        }
                    }else{
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
                }else{
                    System.out.println("  " + role + ":");
                    for (String subRole : subRoleSet) {
                        System.out.println("    " + subRole + ",");
                    }
                }
            }
        }

        for (String group : roleMap.keySet()){
            println("public enum " + group + " {");

            Map<String, Set<String>> secondMap = roleMap.get(group);
            for (String role : secondMap.keySet()){
                Set<String> subRoleSet = secondMap.get(role);
                if (subRoleSet == null) {
                    println(role + "(\"" + group + "." + role + "\")" + ",");
                }else{
                    println("public enum " + role + " {");
                    for (String subRole : subRoleSet) {
                        println(subRole + "(\"" + group + "." + role +  "." + subRole + "\")" + ",");
                    }
                    println("}");
                }
            }
            println("}");
        }


    }

    private void print(String s){
        System.out.print(s);
    }

    private void println(String s){
        System.out.println(s);
    }
}
