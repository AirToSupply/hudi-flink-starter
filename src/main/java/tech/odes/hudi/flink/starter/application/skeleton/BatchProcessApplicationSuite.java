package tech.odes.hudi.flink.starter.application.skeleton;

import tech.odes.hudi.flink.starter.common.config.SourceTable;
import tech.odes.hudi.flink.starter.common.env.BatchContext;
import tech.odes.hudi.flink.starter.common.env.ExecMode;
import tech.odes.hudi.flink.starter.common.env.ExectionContextFactory;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.hudi.configuration.FlinkOptions;
import tech.odes.hudi.flink.starter.common.schema.HoodieTableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BatchProcessApplicationSuite {

    private static Logger LOG = LoggerFactory.getLogger(BatchProcessApplicationSuite.class);

    private static final String SRC_BASE_PATH = "hdfs://172.16.2.120:9000/";

    private static final String SRC_TARGET_PATH = "/hudi/flink/streaming_source_table";

    private static final String SRC_TABLE_NAME = "streaming_source_table";

    private static final String SRC_PARTITION = "partition";

    public static void main(String[] args) throws Exception {

        // step-1: init ExecutionEnvironment and TableEnvironment
        BatchContext batchContext = (BatchContext) ExectionContextFactory.getExectionContext(ExecMode.BATCH);

        StreamExecutionEnvironment execEnv = batchContext.getStreamExecutionEnvironment();
        TableEnvironment tableEnv = batchContext.getTableEnvironment();

        // step-2: hudi table parameter setting，Related parameters can be referenced:
        //         https://hudi.apache.org/docs/configurations.html#flink-options
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(FlinkOptions.PARTITION_PATH_FIELD.key(), SRC_PARTITION);

        // step-3: instant hudi source table
        SourceTable sourceTable = SourceTable.builder()
                // The base path of Hudi table in HDFS
                .basePath(SRC_BASE_PATH)
                // The name of the database where the Hudi table is located
                .targetPath(SRC_TARGET_PATH)
                // The name of the Hudi table
                .name(SRC_TABLE_NAME)
                // hudi table configuration about flink sql
                .conf(conf)
                .build();

        // step-4: Register the current hudi table in the Flink Catalog
        HoodieTableRegistry.connect(tableEnv, sourceTable);

        // step-5: TODO to do something about your processing logic, here is just a simple query
        tableEnv.executeSql("select * from " + SRC_TABLE_NAME).print();
    }
}
