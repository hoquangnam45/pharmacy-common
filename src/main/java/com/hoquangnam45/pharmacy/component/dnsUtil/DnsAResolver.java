package com.hoquangnam45.pharmacy.component.dnsUtil;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.lookup.LookupSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DnsAResolver implements IDnsResolver {
  @Override
  public List<String> resolveDns(String link) throws InterruptedException, ExecutionException, TextParseException {
    List<String> urlParts = splitUrl(link);
    String urlPrefix = urlParts.get(0);
    String urlDomain = urlParts.get(1);
    String urlPort = urlParts.get(2);
    String urlPath = urlParts.get(3);

    LookupSession s = LookupSession.defaultBuilder().build();
    Name aLookup = Name.fromString(urlDomain);
    log.trace("Lookup A record: " + aLookup);

    return s.lookupAsync(aLookup, Type.A)
        .handle((result, ex) -> {
          if (ex != null) {
            log.error(ex.getMessage(), ex);
            return List.<String>of();
          }
          if (result.getRecords().isEmpty()) {
            log.trace(aLookup + " has no record");
            return List.<String>of();
          }
          return result.getRecords().stream()
              .map(rec -> (ARecord) rec)
              .map(rec -> rec.getAddress().getHostAddress())
              .map(url -> joinUrl(urlPrefix, url, urlPort, urlPath))
              .map(url -> {
                log.trace(link + " -> " + url);
                return url;
              })
              .collect(Collectors.toUnmodifiableList());
        })
        .toCompletableFuture()
        .get();
  }
}