package com.jentfoo.server.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
  private static final int MAX_CONCURRENT_REQUESTS = 100000;
  
  public static void main(String[] args) {
    
  }
  
  private final InetSocketAddress bindAddress;
  private final RequestHandler requestHandler;
  
  public HttpServer(int port) {
    this(new InetSocketAddress(port));
  }
  
  public HttpServer(int port, String bindHost) {
    this(new InetSocketAddress(bindHost, port));
  }
  
  public HttpServer(InetSocketAddress bindAddress) {
    if (bindAddress == null) {
      throw new IllegalArgumentException("Must provide InetSocketAddress to bind on");
    }
    
    this.bindAddress = bindAddress;
    this.requestHandler = new RequestHandler(MAX_CONCURRENT_REQUESTS);
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
