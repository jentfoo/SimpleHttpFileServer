package com.jentfoo.server.http;

import java.net.Socket;

import org.threadly.concurrent.PriorityScheduledExecutor;

public class RequestHandler {
  private static final int THREAD_KEEP_ALIVE_TIME_IN_MILLIS = 1000 * 15;
  
  private final PriorityScheduledExecutor scheduler;
  
  protected RequestHandler(int maxConcurrentRequests) {
    int corePoolSize = Math.min(maxConcurrentRequests, Runtime.getRuntime().availableProcessors() * 2);
    scheduler = new PriorityScheduledExecutor(corePoolSize, maxConcurrentRequests, 
                                              THREAD_KEEP_ALIVE_TIME_IN_MILLIS);
  }

  public void handleRequest(Socket requestSocket) {
    scheduler.execute(new RequestWorker(requestSocket));
  }
  
  private static class RequestWorker implements Runnable {
    private final Socket requestSocket;

    public RequestWorker(Socket requestSocket) {
      this.requestSocket = requestSocket;
    }

    @Override
    public void run() {
      // TODO Auto-generated method stub
      
    }
  }
}
