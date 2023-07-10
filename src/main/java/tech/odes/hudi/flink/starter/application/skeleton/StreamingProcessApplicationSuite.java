package tech.odes.hudi.flink.starter.application.skeleton;

import tech.odes.hudi.flink.starter.common.config.SourceTable;
import tech.odes.hudi.flink.starter.common.env.ExecMode;
import tech.odes.hudi.flink.starter.common.env.ExectionContextFactory;
import tech.odes.hudi.flink.starter.common.env.StreamContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.hudi.configuration.FlinkOptions;
import tech.odes.hudi.flink.starter.common.schema.HoodieTableRegistry;

import java.util.HashMap;
import java.util.Map;

public class StreamingProcessApplicationSuite {

    private static final String SRC_BASE_PATH = "hdfs://172.16.2.120:9000/";

    private static final String SRC_TARGET_PATH = "/hudi/flink/streaming_sink_table";

    private static final String SRC_TABLE_NAME = "streaming_sink_table";

    private static final String SRC_PARTITION = "partition";

    private static final Configuration SRC_CONF = new Configuration();

    public static void main(String[] args) {

        // step-1: init ExecutionEnvironment and TableEnvironment
        StreamContext exectionContext = (StreamContext) ExectionContextFactory.getExectionContext(ExecMode.STREAM);

        StreamExecutionEnvironment execEnv = exectionContext.getStreamExecutionEnvironment();
        StreamTableEnvironment tableEnv = exectionContext.getStreamTableEnvironment();

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

        tableEnv.executeSql("select * from " + SRC_TABLE_NAME).print();
        //Table table = tableEnv.sqlQuery("select * from " + SRC_TABLE_NAME);

        // show schema
        //table.printSchema();

        // step-5: TODO to do something about your processing logic, here is just a simple aggregate query
        /*
        Table groupBy = tableEnv.sqlQuery("select uuid, sum(age) as age from t1 group by uuid");
        TupleTypeInfo<Tuple2<String, Integer>> tupleType = new TupleTypeInfo<>(
                Types.STRING(),
                Types.INT());
        DataStream<Tuple2<Boolean, Tuple2<String, Integer>>> dsRow = tableEnv.toRetractStream(groupBy, tupleType);
        tableEnv.registerDataStream("t2", dsRow);
        tableEnv.executeSql("select * from t2").print();
         */
    }
}
