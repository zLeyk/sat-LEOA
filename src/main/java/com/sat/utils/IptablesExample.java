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
        String command = String.format("iptables -A INPUT -s %s -p tcp --dport %d -j DROP", ip, port);
        executeCommand(command);
        System.out.println("已禁止IP端口：" + ip + ":" + port);
    }

    @Override
    public void openIPPort(String ip, int port) {
        String command = String.format("iptables -A INPUT -s %s -p tcp --dport %d -j ACCEPT", ip, port);
        executeCommand(command);
    }

    @Override
    public void openSelfPort( int port) {
        String command = String.format("ip6tables -I INPUT -p tcp --dport %d -j ACCEPT",  port);
        executeCommand(command);
    }

    @Override
    public void unblockIPPort(String ip, int port) {
        String command = String.format("iptables -D INPUT -s %s -p tcp --dport %d -j DROP", ip, port);
        executeCommand(command);
        System.out.println("已开放IP端口：" + ip + ":" + port);
    }

    private void executeCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
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