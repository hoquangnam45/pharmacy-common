package com.hoquangnam45.pharmacy.config;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoquangnam45.pharmacy.component.CloudmapEurekaHandler;
import com.hoquangnam45.pharmacy.component.EcsEurekaClientConfigBean;
import com.hoquangnam45.pharmacy.component.EcsEurekaInstanceConfigBean;
import com.hoquangnam45.pharmacy.component.EcsServiceHostNameHelper;
import com.hoquangnam45.pharmacy.component.dnsUtil.DnsAResolver;
import com.hoquangnam45.pharmacy.component.dnsUtil.DnsSrvResolver;

@Configuration
public class CommonConfiguration {
  @Bean
  @ConditionalOnMissingBean(DnsAResolver.class)
  DnsAResolver dnsAResolver() {
    return new DnsAResolver();
  }

  @Bean
  @ConditionalOnMissingBean(DnsSrvResolver.class)
  DnsSrvResolver dnsSrvResolver() {
    return new DnsSrvResolver();
  }

  @Bean
  @ConditionalOnMissingBean(CloudmapEurekaHandler.class)
  CloudmapEurekaHandler cloudmapEurekaHandler(DnsAResolver dnsAResolver, DnsSrvResolver dnsSrvResolver) {
    return new CloudmapEurekaHandler(dnsAResolver, dnsSrvResolver);
  }

  @Bean
  @ConditionalOnMissingBean(EcsServiceHostNameHelper.class)
  EcsServiceHostNameHelper ecsServiceHostNameHelper(ObjectMapper objectMapper) {
    return new EcsServiceHostNameHelper(objectMapper);
  }

  @Bean
  @ConditionalOnProperty(value = "pharmacy.eureka.server.resolveDnsWithCloudMap", havingValue = "true")
  EcsEurekaClientConfigBean ecsEurekaClientConfigBean(CloudmapEurekaHandler eurekaHandler) {
    return new EcsEurekaClientConfigBean(eurekaHandler);
  }

  @Bean
  @ConditionalOnProperty(value = "pharmacy.eureka.instance.useEcsMetadataHostIp", havingValue = "true")
  EcsEurekaInstanceConfigBean ecsEurekaInstanceConfigBean(InetUtils inetUtils,
      EcsServiceHostNameHelper ecsServiceHostNameHelper, ServerProperties serverProperties)
      throws StreamReadException, DatabindException, IOException {
    return new EcsEurekaInstanceConfigBean(inetUtils, ecsServiceHostNameHelper, serverProperties);
  }
}
