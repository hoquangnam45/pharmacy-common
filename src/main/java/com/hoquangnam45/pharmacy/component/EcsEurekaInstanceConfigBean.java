package com.hoquangnam45.pharmacy.component;

import java.io.IOException;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EcsEurekaInstanceConfigBean extends EurekaInstanceConfigBean {
  private static final Integer CONTAINER_METADATA_TIMEOUT_IN_SECOND = 2;
  private final EcsServiceHostNameHelper ecsServiceHostNameHelper;

  private Map<String, Object> metadata;
  private String advertiseIp;
  private int advertisePort;

  public EcsEurekaInstanceConfigBean(
      InetUtils inetUtils,
      EcsServiceHostNameHelper ecsServiceHostNameHelper,
      ServerProperties serverProperties) throws StreamReadException, DatabindException, IOException {
    super(inetUtils);
    this.ecsServiceHostNameHelper = ecsServiceHostNameHelper;
    this.metadata = ecsServiceHostNameHelper.getContainerMetadata(CONTAINER_METADATA_TIMEOUT_IN_SECOND);
    this.advertiseIp = ecsServiceHostNameHelper.getContainerHostIp(metadata);
    this.advertisePort = ecsServiceHostNameHelper.getContainerHostPort(metadata, serverProperties.getPort());
  }

  @Override
  public String getHostName(boolean refresh) {
    try {
      if (refresh) {
        this.metadata = ecsServiceHostNameHelper.getContainerMetadata(CONTAINER_METADATA_TIMEOUT_IN_SECOND);
        this.advertiseIp = ecsServiceHostNameHelper.getContainerHostIp(metadata);
      }
      log.trace("Ecs service container host ip: " + advertiseIp);
      return advertiseIp;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return null;
    }
  }

  @Override
  public boolean getSecurePortEnabled() {
    return false;
  }

  @Override
  public int getNonSecurePort() {
    return this.advertisePort;
  }
}
