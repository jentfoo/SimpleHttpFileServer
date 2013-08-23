package com.jentfoo.server.http;

import java.io.File;

public class HttpResponseHeaderBuilder {
  private static final String HTTP_VERSION = "HTTP/1.1";
  private static final String NEW_LINE = "\r\n";
  private static final String HEADER_CONNECTION_CLOSED = "Connection: close";
  private static final String HEADER_KEY_CONTENT_TYPE = "Content-Type:";
  private static final String HEADER_KEY_CONTENT_LENGTH = "Content-Length:";
  private static final char _ = ' ';
  
  public static final int CODE_OK = 200;
  public static final int CODE_BAD_REQUEST = 400;
  public static final int CODE_FORBIDDEN = 403;
  public static final int CODE_NOT_FOUND = 404;
  public static final int CODE_SERVER_ERROR = 500;
  public static final int CODE_NOT_IMPLEMENTED = 501;
  public static final int CODE_SERVICE_UNAVAILABLE = 503;

  public static final String CONTENT_TYPE_VIDEO_MP4 = "video/mp4";
  public static final String CONTENT_TYPE_VIDEO_WEBM = "video/webm";
  public static final String CONTENT_TYPE_VIDEO_MATROSKA = "video/x-matroska";
  public static final String CONTENT_TYPE_VIDEO_WMV = "video/x-ms-wmv";
  public static final String CONTENT_TYPE_VIDEO_FLV = "video/x-flv";
  public static final String CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";
  public static final String CONTENT_TYPE_IMAGE_GIF = "image/gif";
  public static final String CONTENT_TYPE_IMAGE_PNG = "image/png";
  public static final String CONTENT_TYPE_IMAGE_TIFF = "image/tiff";
  public static final String CONTENT_TYPE_ZIP = "application/x-zip-compressed";
  public static final String CONTENT_TYPE_TEXT_HTML = "text/html";
  public static final String CONTENT_TYPE_TEXT_XML = "text/xml";
  public static final String CONTENT_TYPE_APPLICATION_STREAM = "application/octet-stream";
  
  public static String buildHeader(int responseCode) {
    return doBuildHeader(responseCode, null, true).toString();
  }
  
  public static String buildHeader(int responseCode, File resultFile) {
    StringBuilder header = doBuildHeader(responseCode, 
                                         getContentType(resultFile), 
                                         false);
    
    header.append(HEADER_KEY_CONTENT_LENGTH)
          .append(_).append(resultFile.length())
          .append(NEW_LINE);
    
    header.append(NEW_LINE);  // end of header
    
    return header.toString();
  }
  
  private static String getContentType(File resultFile) {
    String name = resultFile.getName().toLowerCase();
    if (name.endsWith(".mp4")) {
      return CONTENT_TYPE_VIDEO_MP4;
    } else if (name.endsWith(".webm")) {
      return CONTENT_TYPE_VIDEO_WEBM;
    } else if (name.endsWith(".mkv")) {
      return CONTENT_TYPE_VIDEO_MATROSKA;
    } else if (name.endsWith(".flv")) {
      return CONTENT_TYPE_VIDEO_FLV;
    } else if (name.endsWith(".jpeg") || name.endsWith(".jpg")) {
      return CONTENT_TYPE_IMAGE_JPEG;
    } else if (name.endsWith(".gif")) {
      return CONTENT_TYPE_IMAGE_GIF;
    } else if (name.endsWith(".png")) {
      return CONTENT_TYPE_IMAGE_PNG;
    } else if (name.endsWith(".tiff")) {
      return CONTENT_TYPE_IMAGE_TIFF;
    } else if (name.endsWith(".zip")) {
      return CONTENT_TYPE_ZIP;
    } else if (name.endsWith(".html") || name.endsWith(".htm")) {
      return CONTENT_TYPE_TEXT_HTML;
    } else if (name.endsWith(".xml")) {
      return CONTENT_TYPE_TEXT_XML;
    } else {
      return CONTENT_TYPE_APPLICATION_STREAM;
    }
  }

  private static StringBuilder doBuildHeader(int responseCode, String contentType, boolean endHeader) {
    StringBuilder result = new StringBuilder();
    result.append(HTTP_VERSION).append(_);
    
    switch (responseCode) {
      case CODE_OK:
        result.append("200 OK");
        break;
      case CODE_BAD_REQUEST:
        result.append("400 Bad Request");
        break;
      case CODE_FORBIDDEN:
        result.append("403 Forbidden");
        break;
      case CODE_NOT_FOUND:
        result.append("404 Not Found");
        break;
      case CODE_SERVER_ERROR:
        result.append("500 Internal Server Error");
        break;
      case CODE_NOT_IMPLEMENTED:
        result.append("501 Not Implemented");
        break;
      case CODE_SERVICE_UNAVAILABLE:
        result.append("503 Service unavailable");
        break;
      default:
        throw new UnsupportedOperationException("Response code not handled: " + 
                                                  responseCode);
    }
    result.append(NEW_LINE);
    
    result.append(HEADER_CONNECTION_CLOSED).append(NEW_LINE);
    if (contentType != null) {
      contentType = contentType.trim();
      if (contentType.length() > 0) {
        if (contentType.startsWith(HEADER_KEY_CONTENT_TYPE)) {
          result.append(contentType).append(NEW_LINE);;
        } else {
          result.append(HEADER_KEY_CONTENT_TYPE).append(_).append(NEW_LINE);
        }
      }
    }
    
    if (endHeader) {
      result.append(NEW_LINE);  // mark end of headers
    }
    
    return result;
  }
}
