package com.hoquangnam45.pharmacy.component.dnsUtil;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.xbill.DNS.TextParseException;

public interface IDnsResolver {
  List<String> resolveDns(String link) throws InterruptedException, ExecutionException, TextParseException;

  default List<String> splitUrl(String link) {
    String[] protocolSplit = link.split("://", 2);
    String urlPrefix = "";
    String urlDomain = "";
    String urlPort = "";
    String urlPath = "";

    if (protocolSplit.length == 2) {
      urlPrefix = protocolSplit[0];
      urlDomain = protocolSplit[1];
    } else {
      urlDomain = protocolSplit[0];
    }

    String[] pathSplit = urlDomain.split("/", 2);

    if (pathSplit.length == 2) {
      urlDomain = pathSplit[0];
      urlPath = pathSplit[1];
    } else {
      urlDomain = pathSplit[0];
    }

    String[] portSplit = urlDomain.split(":", 2);
    if (portSplit.length == 2) {
      urlDomain = portSplit[0];
      urlPort = portSplit[1];
    } else {
      urlDomain = pathSplit[0];
    }
    return List.of(urlPrefix, urlDomain, urlPort, urlPath);
  }

  default String joinUrl(String urlPrefix, String urlDomain, String urlPort, String urlPath) {
    String url = "";
    if (urlPrefix != null && !urlPrefix.isBlank()) {
      url += urlPrefix + "://";
    }
    url += urlDomain;
    if (urlPort != null && !urlPort.isBlank()) {
      url += ":" + urlPort;
    }
    if (urlPath != null && !urlPath.isBlank()) {
      url += "/" + urlPath;
    }
    return url;
  }
}
