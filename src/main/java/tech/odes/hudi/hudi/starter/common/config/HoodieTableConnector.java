package tech.odes.hudi.hudi.starter.common.config;

import org.apache.flink.configuration.Configuration;

public interface HoodieTableConnector {

    String getBasePath();

    String getTargetPath();

    String getName();

    String getTablePath();

    Configuration getConfiguration();
}
