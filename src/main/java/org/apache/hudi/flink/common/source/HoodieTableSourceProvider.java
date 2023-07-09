package org.apache.hudi.flink.common.source;

import org.apache.flink.table.api.TableSchema;
import org.apache.hudi.flink.common.schema.HoodieTableDescriptor;
import org.apache.hudi.flink.common.schema.HoodieTableSchemaHelper;
import org.apache.hudi.table.HoodieTableSource;

public class HoodieTableSourceProvider {

    private static String DEFAULT_PARTNAME = "default-par";

    public static HoodieTableSource buildSource(HoodieTableDescriptor descriptor) {

        TableSchema tableSchema = HoodieTableSchemaHelper.getTableSchemaByTablePath(descriptor.getTablePath());

        return new HoodieTableSource(tableSchema,
                new org.apache.hadoop.fs.Path(descriptor.getTablePath()),
                descriptor.getPartitionKeys(),
                DEFAULT_PARTNAME,
                descriptor.getConf());
    }
}
