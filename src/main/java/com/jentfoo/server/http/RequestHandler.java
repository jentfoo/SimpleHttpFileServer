package com.jentfoo.server.http;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.threadly.concurrent.PriorityScheduledExecutor;

public class RequestHandler {
  private static final int THREAD_KEEP_ALIVE_TIME_IN_MILLIS = 1000 * 15;
  private static final String GET_STR = "GET";
  private static final String POST_STR = "POST";
  private static final int BUFFER_SIZE = 2048;
  
  private final File fileRoot;
  private final PriorityScheduledExecutor scheduler;
  
  protected RequestHandler(int maxConcurrentRequests, File fileRoot) throws IOException {
    if (maxConcurrentRequests < 1) {
      throw new IllegalArgumentException("Must allow at least one concurrent request");
    } else if (fileRoot == null) {
      throw new IllegalArgumentException("Must provide file root to serve files from");
    } else if (! fileRoot.exists()) {
      throw new IllegalArgumentException("File root does not exist: " + fileRoot.getAbsolutePath());
    } else if (! fileRoot.isDirectory()) {
      throw new IllegalArgumentException("File root must be a directory: " + fileRoot.getAbsolutePath());
    }
    
    this.fileRoot = fileRoot.getCanonicalFile();
    
    int corePoolSize = Math.min(maxConcurrentRequests, Runtime.getRuntime().availableProcessors() * 2);
    scheduler = new PriorityScheduledExecutor(corePoolSize, maxConcurrentRequests, 
                                              THREAD_KEEP_ALIVE_TIME_IN_MILLIS);
  }

  public void handleRequest(Socket requestSocket) throws IOException {
    scheduler.execute(new RequestWorker(requestSocket));
  }
  
  private class RequestWorker implements Runnable {
    private final Socket requestSocket;
    private final InputStream requestIn;
    private final OutputStream requestOut;
    private String requestHeader;

    public RequestWorker(Socket requestSocket) throws IOException {
      this.requestSocket = requestSocket;
      requestIn = requestSocket.getInputStream();
      requestOut = requestSocket.getOutputStream();
      requestHeader = null;
    }

    @Override
    public void run() {
      try {
        System.out.println("Got request from: " + requestSocket.getRemoteSocketAddress());
        String request = getRequest();
        if (request.startsWith(GET_STR)) {
          int requestStartPoint = GET_STR.length() + 2;
          if (request.length() < requestStartPoint) {
            throw new UnsupportedOperationException("No requested file");
          }
          
          String requestedFileStr = request.substring(requestStartPoint);
          int requstedFileEnd = requestedFileStr.indexOf(" HTTP");
          if (requstedFileEnd < 0) {
            throw new IllegalStateException("Http version not provided in get request");
          }
          requestedFileStr = requestedFileStr.substring(0, requstedFileEnd);
          
          System.out.println("requested: " + requestedFileStr);
          
          File requestedFile = new File(fileRoot, requestedFileStr).getCanonicalFile();
          if (! requestedFile.getAbsolutePath().contains(fileRoot.getAbsolutePath()) || 
              ! requestedFile.canRead()) {
            String header = HttpResponseHeaderBuilder.buildHeader(HttpResponseHeaderBuilder.CODE_FORBIDDEN);
            requestOut.write(header.getBytes());
          } else if (! requestedFile.exists()) {
            String header = HttpResponseHeaderBuilder.buildHeader(HttpResponseHeaderBuilder.CODE_NOT_FOUND);
            requestOut.write(header.getBytes());
          } else {
            String header = HttpResponseHeaderBuilder.buildHeader(HttpResponseHeaderBuilder.CODE_OK, 
                                                                  requestedFile);
            requestOut.write(header.getBytes());
            
            // now write file
            InputStream fileIn = new FileInputStream(requestedFile);
            try {
              byte[] buffer = new byte[BUFFER_SIZE];
              int readCount;
              while ((readCount = fileIn.read(buffer)) != -1) {
                requestOut.write(buffer, 0, readCount);
              }
            } finally {
              fileIn.close();
            }
          }
        } else if (request.startsWith(POST_STR)) {
          throw new UnsupportedOperationException("Post requests not currently implemented");
        }
      } catch (IOException e) {
        e.printStackTrace(System.err);
      } finally {
        close();
      }
    }
    
    private String getRequest() throws IOException {
      String requestHeader = getRequestHeader();
      
      return requestHeader.substring(0, requestHeader.indexOf('\n'));
    }
    
    private String getRequestHeader() throws IOException {
      if (requestHeader == null) {
        StringBuilder result = new StringBuilder(Math.max(32, requestIn.available()));
        boolean done = false;
        do {
          if (requestIn.available() > 0) {
            byte[] availableData = new byte[requestIn.available()];
            requestIn.read(availableData);
            
            result.append(new String(availableData));
            
            done = headerComplete(result);
          } else {
            int val = requestIn.read();
            System.out.print((char)val);
            result.append((char)val);
            
            done = headerComplete(result);
            if (! done && val == -1) {
              throw new EOFException("Incomplete request header: \n" + result.toString());
            }
          }
        } while (! done);
        
        requestHeader = result.toString();
      }
      
      return requestHeader;
    }
    
    private boolean headerComplete(StringBuilder currentHeader) {
      return currentHeader.indexOf("\r\n\r\n") >= 0 || currentHeader.indexOf("\n\n") >= 0;
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
