package org.apache.hudi.flink.application.programe;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.Types;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.data.RowData;
import org.apache.hudi.flink.common.env.ExecMode;
import org.apache.hudi.flink.common.env.ExectionContextFactory;
import org.apache.hudi.flink.common.env.StreamContext;
import org.apache.hudi.flink.common.schema.HoodieTableDescriptor;
import org.apache.hudi.flink.common.source.HoodieTableSourceProvider;
import org.apache.hudi.table.HoodieTableSource;

import java.util.Arrays;

public class StreamingProcessApplicationSuite {

    private static final String SRC_BASE_PATH = "hdfs://172.16.2.120:9000/hudi";

    private static final String SRC_NAMESPACE = "flink";

    private static final String SRC_TABLE_NAME = "t1";

    private static final String SRC_PARTITION = "partition";

    private static final Configuration SRC_CONF = new Configuration();

    public static void main(String[] args) {

        StreamContext exectionContext = (StreamContext) ExectionContextFactory.getExectionContext(ExecMode.STREAM);

        StreamExecutionEnvironment execEnv = exectionContext.getStreamExecutionEnvironment();
        StreamTableEnvironment tableEnv = exectionContext.getStreamTableEnvironment();

        // define hudi table descriptor
        HoodieTableDescriptor hoodieTableDescriptor = new HoodieTableDescriptor(
                SRC_BASE_PATH,
                SRC_NAMESPACE,
                SRC_TABLE_NAME,
                Arrays.asList(SRC_PARTITION),
                SRC_CONF);

        // instant HoodieTableSource
        HoodieTableSource tableSource = HoodieTableSourceProvider.buildSource(hoodieTableDescriptor);

        // table source to DataStream
        DataStream<RowData> dataStream = tableSource.getDataStream(execEnv);

        // register hudi table to flink
        tableEnv.registerDataStream("t1", dataStream);
        Table table = tableEnv.fromDataStream(dataStream);

        // show schema
        table.printSchema();

        // TODO to do something
        Table groupBy = tableEnv.sqlQuery("select uuid, sum(age) as age from t1 group by uuid");

        TupleTypeInfo<Tuple2<String, Integer>> tupleType = new TupleTypeInfo<>(
                Types.STRING(),
                Types.INT());

        DataStream<Tuple2<Boolean, Tuple2<String, Integer>>> dsRow = tableEnv.toRetractStream(groupBy, tupleType);

        tableEnv.registerDataStream("t2", dsRow);

        tableEnv.executeSql("select * from t2").print();
    }
}
