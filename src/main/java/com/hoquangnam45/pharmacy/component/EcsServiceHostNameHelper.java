package com.hoquangnam45.pharmacy.component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoquangnam45.pharmacy.constant.EcsConstants;

public class EcsServiceHostNameHelper {
  private final ObjectMapper mapper;

  public EcsServiceHostNameHelper(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public String getContainerHostIp(Map<String, Object> metadata) {
    return (String) metadata.get("HostPrivateIPv4Address");
  }

  @SuppressWarnings("unchecked")
  public Integer getContainerHostPort(Map<String, Object> metadata, Integer containerPort) {
    List<Map<String, Object>> portMappings = (List<Map<String, Object>>) metadata.get("PortMappings");
    return portMappings.stream()
        .map(portMapping -> List.of(
            Integer.valueOf(portMapping.get("ContainerPort").toString()),
            Integer.valueOf(portMapping.get("HostPort").toString())))
        .filter(portMapping -> portMapping.get(0).equals(containerPort))
        .findAny()
        .map(portMapping -> portMapping.get(1))
        .orElseThrow(() -> new IllegalStateException(
            "Check task definition again there is no port " + containerPort + " used by this container"));
  }

  public Map<String, Object> getContainerMetadata(Integer timeoutInSecond)
      throws StreamReadException, DatabindException, IOException {
    long startTime = System.currentTimeMillis();
    boolean metadataFileReady = false;
    Map<String, Object> metadata = null;
    while (!metadataFileReady
        || System.currentTimeMillis() - startTime < EcsConstants.SECOND_TO_MILLIS * timeoutInSecond) {
      metadata = mapper.readValue(new File(System.getenv(EcsConstants.ECS_CONTAINER_METADATA_FILE_ENV)),
          new TypeReference<HashMap<String, Object>>() {
          });
      metadataFileReady = "READY".equalsIgnoreCase((String) metadata.getOrDefault("MetadataFileStatus", null));
    }
    if (!metadataFileReady) {
      throw new IllegalStateException(
          "Metadata file is not ready, check ecs agent launch template setup again please :)");
    }
    return metadata;
  }
}
