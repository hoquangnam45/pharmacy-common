package com.hoquangnam45.pharmacy.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hoquangnam45.pharmacy.component.dnsUtil.DnsAResolver;
import com.hoquangnam45.pharmacy.component.dnsUtil.DnsSrvResolver;
import com.hoquangnam45.pharmacy.helper.Functions;

public class CloudmapEurekaHandler {
  private static final String CLOUD_MAP_PREFIX = "cloudMap::";
  private final DnsAResolver dnsAResolver;
  private final DnsSrvResolver dnsSrvResolver;

  public CloudmapEurekaHandler(DnsAResolver dnsAResolver, DnsSrvResolver dnsSrvResolver) {
    this.dnsAResolver = dnsAResolver;
    this.dnsSrvResolver = dnsSrvResolver;
  }

  public Map<String, List<String>> getEurekaServerServiceUrls(List<String> eurekaServerServiceUrls) {
    Map<String, List<String>> resolvedEurekaServiceUrls = new HashMap<>();
    for (String serviceUrl : eurekaServerServiceUrls) {
      if (!serviceUrl.startsWith(CLOUD_MAP_PREFIX)) {
        resolvedEurekaServiceUrls.put(serviceUrl, List.of(serviceUrl));
        continue;
      }
      serviceUrl = serviceUrl.substring(CLOUD_MAP_PREFIX.length());
      List<String> resolvedServiceDiscoveryIps = Stream.of(serviceUrl)
          .flatMap(url -> Functions.rethrow(dnsSrvResolver::resolveDns).apply(url).stream())
          .flatMap(url -> Functions.rethrow(dnsAResolver::resolveDns).apply(url).stream())
          .collect(Collectors.toList());
      resolvedEurekaServiceUrls.put(serviceUrl, resolvedServiceDiscoveryIps);
    }
    return Collections.unmodifiableMap(resolvedEurekaServiceUrls);
  }
}
