package com.jentfoo.server.http;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpFileServer {
  private static final int MAX_CONCURRENT_REQUESTS = 100000;
  
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("Expected at least two arguments: <root file path> <port> [bind host]");
      System.exit(1);
    }
    File fileRoot = new File(args[0]);
    int bindPort = Integer.parseInt(args[1]);
    
    HttpFileServer server;
    if (args.length > 2) {
      server = new HttpFileServer(bindPort, args[2], fileRoot);
    } else {
      server = new HttpFileServer(bindPort, fileRoot);
    }
    
    server.startServer();
  }
  
  private final InetSocketAddress bindAddress;
  private final RequestHandler requestHandler;
  
  public HttpFileServer(int port, File fileRoot) {
    this(new InetSocketAddress(port), fileRoot);
  }
  
  public HttpFileServer(int port, String bindHost, File fileRoot) {
    this(new InetSocketAddress(bindHost, port), fileRoot);
  }
  
  public HttpFileServer(InetSocketAddress bindAddress, File fileRoot) {
    if (bindAddress == null) {
      throw new IllegalArgumentException("Must provide InetSocketAddress to bind on");
    }
    
    this.bindAddress = bindAddress;
    this.requestHandler = new RequestHandler(MAX_CONCURRENT_REQUESTS, fileRoot);
  }
  
  public void startServer() throws IOException {
    ServerSocket serverSocket = new ServerSocket();
    try {
      serverSocket.bind(bindAddress);
      
      if (! serverSocket.isBound()) {
        throw new IOException("Unable to bind to socket");
      }
      
      while (! serverSocket.isClosed() && serverSocket.isBound()) {
        try {
          Socket acceptedSocket = serverSocket.accept();
          
          requestHandler.handleRequest(acceptedSocket);
        } catch (IOException e) {
          e.printStackTrace(System.err);
        }
      }
    } finally {
      serverSocket.close();
    }
  }
}
