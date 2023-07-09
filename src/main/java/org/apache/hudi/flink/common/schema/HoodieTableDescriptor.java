package org.apache.hudi.flink.common.schema;

import org.apache.flink.configuration.Configuration;
import org.apache.hudi.configuration.FlinkOptions;
import org.apache.hudi.util.StreamerUtil;

import java.io.IOException;
import java.util.List;

public class HoodieTableDescriptor {

    private String basePath;

    private String namespace;

    private String tableName;

    private List<String> partitionKeys;

    private Configuration conf;

    public HoodieTableDescriptor(String basePath, String namespace, String tableName) {
        this.basePath = basePath;
        this.namespace = namespace;
        this.tableName = tableName;
    }

    public HoodieTableDescriptor(
            String basePath, String namespace, String tableName, List<String> partitionKeys, Configuration conf) {
        this.basePath = basePath;
        this.namespace = namespace;
        this.tableName = tableName;
        this.partitionKeys = partitionKeys;
        this.conf = conf;
        this.conf.setString(FlinkOptions.PATH, this.getTablePath());
        this.conf.setString(FlinkOptions.TABLE_NAME, this.tableName);
        try {
            StreamerUtil.initTableIfNotExists(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<String> getPartitionKeys() {
        return partitionKeys;
    }

    public void setPartitionKeys(List<String> partitionKeys) {
        this.partitionKeys = partitionKeys;
    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    public String getTablePath() {
        try {
            return HoodieTableSchemaHelper.getTablePath(this.basePath, this.namespace, this.tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
