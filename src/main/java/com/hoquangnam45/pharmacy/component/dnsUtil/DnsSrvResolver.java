package com.hoquangnam45.pharmacy.component.dnsUtil;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.xbill.DNS.Name;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.lookup.LookupSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DnsSrvResolver implements IDnsResolver {
  @Override
  public List<String> resolveDns(String link) throws InterruptedException, ExecutionException, TextParseException {
    List<String> urlParts = splitUrl(link);
    String urlPrefix = urlParts.get(0);
    String urlDomain = urlParts.get(1);
    String urlPath = urlParts.get(3);

    String[] srvUrls = urlDomain.split("\\.", 3);
    String serviceName = srvUrls[0];
    String proto = srvUrls.length >= 2 ? srvUrls[1] : "";
    String domainName = srvUrls.length == 3 ? srvUrls[2] : "";
    String dnsName = serviceName + "." + proto + "." + domainName;
    LookupSession s = LookupSession.defaultBuilder().build();
    Name srvLookup = Name.fromString(dnsName);
    log.trace("Lookup SRV record: " + srvLookup);
    return s.lookupAsync(srvLookup, Type.SRV)
        .handle((result, ex) -> {
          if (ex != null) {
            log.error(ex.getMessage(), ex);
            return List.<String>of();
          }
          if (result.getRecords().isEmpty()) {
            log.trace(srvLookup + " has no record");
            return List.<String>of();
          }
          return result.getRecords().stream()
              .map(rec -> (SRVRecord) rec)
              .map(rec -> joinUrl(urlPrefix, rec.getTarget().toString(true), Integer.toString(rec.getPort()), urlPath))
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