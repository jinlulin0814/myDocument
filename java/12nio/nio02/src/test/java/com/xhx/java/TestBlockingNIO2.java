package com.xhx.java;


import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 */
public class TestBlockingNIO2 {

    @Test
    public void client() throws Exception {
        //1. 获取通道
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

        FileChannel inChannel = FileChannel.open(Paths.get("pom.xml"), StandardOpenOption.READ);

        //2. 分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //3.读取本地文件，并发送到服务器
        while (inChannel.read(buf) != -1) {
            buf.flip();
            sChannel.write(buf);
            buf.clear();
        }
        //shutdown一下，通知服务端发送完数据
        sChannel.shutdownOutput();

        //4. 接收服务端反馈
        while (sChannel.read(buf)!=-1){
            buf.flip();
            System.out.println(new String(buf.array(),0,buf.limit()));
            buf.clear();
        }

        //5. 关闭通道
        inChannel.close();
        sChannel.close();

    }

    @Test
    public void server() throws Exception {
        //1. 获取通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();
        FileChannel outChannel = FileChannel.open(Paths.get("2.pom"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        //2. 绑定连接
        ssChannel.bind(new InetSocketAddress(9898));

        //3. 获取客户端连接的通信
        SocketChannel sChannel = ssChannel.accept();

        //4. 接收客户端的数据，并保存到本地
        ByteBuffer buf = ByteBuffer.allocate(1024);
        while (sChannel.read(buf) != -1) {
            buf.flip();
            outChannel.write(buf);
            buf.clear();
        }

        //5. 发送反馈到客户端
        buf.put("服务端接收成功".getBytes());
        buf.flip();
        sChannel.write(buf);

        sChannel.shutdownOutput();

        //6. 关闭通道
        sChannel.close();
        ssChannel.close();
        outChannel.close();


    }
}
