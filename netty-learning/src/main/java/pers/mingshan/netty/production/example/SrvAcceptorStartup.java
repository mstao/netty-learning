package pers.mingshan.netty.production.example;

import pers.mingshan.netty.production.srv.acceptor.DefaultCommonSrvAcceptor;

public class SrvAcceptorStartup {

    public static void main(String[] args) throws InterruptedException {
        DefaultCommonSrvAcceptor srvAcceptor = new DefaultCommonSrvAcceptor(8078, null);
        srvAcceptor.start();
    }
}
