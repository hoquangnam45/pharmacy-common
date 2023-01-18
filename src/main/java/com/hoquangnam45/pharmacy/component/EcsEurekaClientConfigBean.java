package com.hoquangnam45.pharmacy.component;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EcsEurekaClientConfigBean extends EurekaClientConfigBean {
  private final CloudmapEurekaHandler eurekaHandler;

  public EcsEurekaClientConfigBean(CloudmapEurekaHandler eurekaHandler) {
    this.eurekaHandler = eurekaHandler;
  }

  @Override
  public List<String> getEurekaServerServiceUrls(String myZone) {
    List<String> eurekaServerServiceUrls = super.getEurekaServerServiceUrls(myZone);
    return eurekaHandler.getEurekaServerServiceUrls(eurekaServerServiceUrls).entrySet()
        .stream()
        .flatMap(resolvedEntry -> resolvedEntry.getValue().stream())
        .map(it -> {
          log.trace(it);
          return it;
        })
        .collect(Collectors.toList());
  }
}
