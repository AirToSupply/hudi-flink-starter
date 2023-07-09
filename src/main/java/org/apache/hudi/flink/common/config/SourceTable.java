package org.apache.hudi.flink.common.config;

import org.apache.flink.configuration.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hudi.configuration.FlinkOptions;

import java.util.Map;

public class SourceTable {

    private String basePath;

    private String namespace;

    private String name;

    private Map<String, String> conf;

    public SourceTable() {
    }

    public SourceTable(String basePath, String namespace, String name, Map<String, String> conf) {
        this.basePath = basePath;
        this.namespace = namespace;
        this.name = name;
        this.conf = conf;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getConf() {
        return conf;
    }

    public void setConf(Map<String, String> conf) {
        this.conf = conf;
    }

    public Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        requireConfiguration(configuration);
        this.conf.entrySet().stream().forEach(item -> configuration.setString(item.getKey(), item.getValue()));
        return configuration;
    }

    private void requireConfiguration(Configuration configuration) {
        configuration.setString(FlinkOptions.PATH, getTablePath());
        configuration.setString(FlinkOptions.TABLE_NAME, this.name);
    }

    public String getTablePath() {
        return String.join(Path.SEPARATOR, this.basePath, this.namespace, this.name);
    }
}
