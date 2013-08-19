package com.jentfoo.server.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.threadly.concurrent.PriorityScheduledExecutor;

public class RequestHandler {
  private static final int THREAD_KEEP_ALIVE_TIME_IN_MILLIS = 1000 * 15;
  
  private final File fileRoot;
  private final PriorityScheduledExecutor scheduler;
  
  protected RequestHandler(int maxConcurrentRequests, File fileRoot) {
    if (maxConcurrentRequests < 1) {
      throw new IllegalArgumentException("Must allow at least one concurrent request");
    } else if (fileRoot == null) {
      throw new IllegalArgumentException("Must provide file root to serve files from");
    } else if (! fileRoot.exists()) {
      throw new IllegalArgumentException("File root does not exist: " + fileRoot.getAbsolutePath());
    } else if (! fileRoot.isDirectory()) {
      throw new IllegalArgumentException("File root must be a directory: " + fileRoot.getAbsolutePath());
    }
    
    this.fileRoot = fileRoot;
    
    int corePoolSize = Math.min(maxConcurrentRequests, Runtime.getRuntime().availableProcessors() * 2);
    scheduler = new PriorityScheduledExecutor(corePoolSize, maxConcurrentRequests, 
                                              THREAD_KEEP_ALIVE_TIME_IN_MILLIS);
  }

  public void handleRequest(Socket requestSocket) throws IOException {
    scheduler.execute(new RequestWorker(requestSocket));
  }
  
  private static class RequestWorker implements Runnable {
    private final Socket requestSocket;
    private final InputStream requestIn;
    private final OutputStream requestOut;

    public RequestWorker(Socket requestSocket) throws IOException {
      this.requestSocket = requestSocket;
      requestIn = requestSocket.getInputStream();
      requestOut = requestSocket.getOutputStream();
    }

    @Override
    public void run() {
      try {
        System.out.println("Got request from: " + requestSocket.getRemoteSocketAddress());
        int val;
        while ((val = requestIn.read()) != -1) {
          System.out.print((char)val);
        }
      } catch (IOException e) {
          e.printStackTrace(System.err);
      } finally {
        close();
      }
    }
    
    private void close() {
      try {
        requestIn.close();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
      try {
        requestOut.close();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
      try {
        requestSocket.close();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    }
  }
}
