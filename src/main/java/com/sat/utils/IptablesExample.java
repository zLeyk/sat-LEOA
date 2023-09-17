package com.sat.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class IptablesExample implements IptablesManager {
    /*public static void main(String[] args) {
        String ip = "192.168.0.100";
        int port = 8080;

        IptablesManager iptablesManager = new IptablesExample();

        // 禁止IP端口
        iptablesManager.blockIPPort(ip, port);

        // 解禁IP端口
        iptablesManager.unblockIPPort(ip, port);

        // 开放IP端口
        iptablesManager.openIPPort(ip, port);
    }*/

    @Override
    public void blockIPPort(String ip, int port) {
        String command = String.format("iptables -I INPUT -s %s -p tcp --dport %d -j DROP", ip, port);
        executeCommand(command);
        System.out.println("已禁止IP端口：" + ip + ":" + port);
    }

    @Override
    public void openIPPort(String ip, int port) {
        String command = String.format("iptables -I INPUT -s %s -p tcp --dport %d -j ACCEPT", ip, port);
        executeCommand(command);
    }

    @Override
    public void unblockIPPort(String ip, int port) {
        String command = String.format("iptables -D INPUT -s %s -p tcp --dport %d -j DROP", ip, port);
        executeCommand(command);
        System.out.println("已开放IP端口：" + ip + ":" + port);
    }

    @Override
    public void blockIcmp(String ip) {
        String command = String.format("sudo ip6tables -I INPUT -p icmpv6 -m icmp6 --icmpv6-type 128 -s %s -j DROP", ip );
        String ruleToCheck = "DROP       ipv6-icmp    " + ip + "  anywhere             ipv6-icmp echo-request";

        if (!isIp6tablesRuleExists(ruleToCheck)) {
            executeCommand(command);
            System.out.println("已禁止IP：" + ip );
        } else {
            System.out.println(ruleToCheck+"规则已存在，不需要再次添加。");
        }

        /*ACCEPT     ipv6-icmp    fd15:4ba5:5a2b:1008:41cb:1c49:615b:51b3  anywhere             ipv6-icmp echo-request
        DROP       ipv6-icmp    anywhere             anywhere             ipv6-icmp echo-request*/



    }

    @Override
    public void blockALLIcmp() {
        String command = String.format("sudo ip6tables -I INPUT -p icmpv6 -m icmp6 --icmpv6-type 128 -j DROP");
        String ruleToCheck = "DROP       ipv6-icmp    anywhere             anywhere             ipv6-icmp echo-request";

        if (!isIp6tablesRuleExists(ruleToCheck)) {
            executeCommand(command);
            System.out.println("已禁止所有IP：" );
        } else {
            System.out.println(ruleToCheck+"规则已存在，不需要再次添加。");
        }

    }

    @Override
    public void acceptIcmp(String ip) {//单个ping的通行规则

        String command = String.format("sudo ip6tables -I INPUT -p icmpv6 -m icmp6 --icmpv6-type 128 -s %s -j ACCEPT", ip );
        //String ruleToCheck = "ACCEPT       ipv6-icmp    " + ip + "  anywhere             ipv6-icmp echo-request";
        //String ruleToCheck = "ACCEPT     ipv6-icmp    "+ ip + "  anywhere             ipv6-icmp echo-request";
        String ruleToCheck = "ACCEPT     ipv6-icmp    "+ ip + "          anywhere             ipv6-icmp echo-request";
        if (!isIp6tablesRuleExists(ruleToCheck)) {
            executeCommand(command);
            System.out.println("已接收IP："  + ip );
        } else {
            System.out.println(ruleToCheck+"规则已存在，不需要再次添加。");
        }

    }

    @Override
    public void DacceptIcmp(String ip) {//删掉单个ping的通行规则
        String command = String.format("sudo ip6tables -D INPUT -p icmpv6 -m icmp6 --icmpv6-type 128 -s %s -j ACCEPT", ip );
        executeCommand(command);
        System.out.println("已禁止IP：" + ip );
    }

    @Override
    public void openIcmp(String ip) {
        String command = String.format("sudo ip6tables -D INPUT -p icmpv6 -m icmp6 --icmpv6-type 128 -s %s -j DROP", ip );
        executeCommand(command);
        System.out.println("已开放IP：" + ip );
    }
    @Override
    public void blockTcpTemp(String ip) {
        String command = String.format("sudo firewall-cmd --zone=public --add-rich-rule=\"rule family=\"ipv6\" source address=\"%s\" port protocol=\"tcp\" port=\"8899\" drop\" --timeout=60", ip );
        executeCommand(command);
        System.out.println(command);
        System.out.println("已临时封禁IP：" + ip );
    }

    @Override
    public void openSelfPort( int port) {
        String command = String.format("ip6tables -I INPUT -p tcp --dport %d -j ACCEPT",  port);
        executeCommand(command);
    }

    private boolean isIp6tablesRuleExists(String ruleToCheck) {
        try {
            Process process = Runtime.getRuntime().exec("sudo ip6tables -L");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                if (line.contains(ruleToCheck)) {
                    return true;
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;

    }

    private void executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();

            BufferedReader errorReader = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String error = null;
            while((error = errorReader.readLine()) != null){
                System.out.println(error);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("执行命令时出现错误，错误代码: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}