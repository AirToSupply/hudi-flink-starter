package org.apache.hudi.flink.application.programe;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.internal.TableEnvironmentInternal;
import org.apache.hudi.configuration.FlinkOptions;
import org.apache.hudi.flink.common.env.BatchContext;
import org.apache.hudi.flink.common.env.ExecMode;
import org.apache.hudi.flink.common.env.ExectionContextFactory;
import org.apache.hudi.flink.common.schema.HoodieTableDescriptor;
import org.apache.hudi.flink.common.source.HoodieTableSourceProvider;
import org.apache.hudi.table.HoodieTableSink;
import org.apache.hudi.table.HoodieTableSource;
import org.apache.hudi.util.AvroSchemaConverter;
import org.apache.hudi.util.StreamerUtil;

import java.util.Arrays;

public class BatchProcessApplicationSuite {

    private static final String SRC_BASE_PATH = "hdfs://172.16.2.120:9000/hudi";

    private static final String SRC_NAMESPACE = "flink";

    private static final String SRC_TABLE_NAME = "t2";

    private static final String SRC_PARTITION = "partition";

    private static final Configuration SRC_CONF = new Configuration();

    public static void main(String[] args) {

        BatchContext batchContext = (BatchContext) ExectionContextFactory.getExectionContext(ExecMode.BATCH);

        StreamExecutionEnvironment execEnv = batchContext.getStreamExecutionEnvironment();
        TableEnvironment tableEnv = batchContext.getTableEnvironment();

        // define hudi table descriptor
        HoodieTableDescriptor hoodieTableDescriptor = new HoodieTableDescriptor(
                SRC_BASE_PATH,
                SRC_NAMESPACE,
                SRC_TABLE_NAME,
                Arrays.asList(SRC_PARTITION),
                SRC_CONF);

        // instant HoodieTableSource
        HoodieTableSource tableSource = HoodieTableSourceProvider.buildSource(hoodieTableDescriptor);

        // register hudi table to flink
        Table table = tableEnv.fromTableSource(tableSource);

        // Note: flink 1.12.x TableEnvironment need to TableEnvironmentInternal can work
        ((TableEnvironmentInternal)tableEnv).registerTableSourceInternal(SRC_TABLE_NAME, tableSource);

        // show schema
        table.printSchema();

        // TODO to do something
        tableEnv.executeSql("select * from t2").print();
    }
}
