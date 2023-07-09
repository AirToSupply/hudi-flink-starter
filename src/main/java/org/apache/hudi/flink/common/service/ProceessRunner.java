package org.apache.hudi.flink.common.service;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.sql.parser.dml.RichSqlInsert;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.api.internal.TableEnvironmentInternal;
import org.apache.hadoop.fs.Path;
import org.apache.hudi.configuration.FlinkOptions;
import org.apache.hudi.flink.common.config.ApplicationConfig;
import org.apache.hudi.flink.common.config.SinkTable;
import org.apache.hudi.flink.common.config.SourceTable;
import org.apache.hudi.flink.common.parser.FlinkSqlParser;
import org.apache.hudi.flink.common.schema.HoodieTableSchemaHelper;
import org.apache.hudi.table.HoodieTableSink;
import org.apache.hudi.table.HoodieTableSource;
import org.apache.hudi.util.AvroSchemaConverter;
import org.apache.hudi.util.StreamerUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProceessRunner {

    private static String DEFAULT_PARTNAME = "default-par";

    private static String SCRIPT_SPLIT_CHAR = ";\\n";

    private static String SQL_EOF = ";";

    private static void registeSource(TableEnvironment tableEnv, SourceTable sourceTable) {
        // 1. obtain hudi table schema by storage location
        TableSchema tableSchema = HoodieTableSchemaHelper.getTableSchemaByTablePath(sourceTable.getTablePath());

        // 2. instant HoodieTableSource
        Configuration conf = sourceTable.getConfiguration();
        HoodieTableSource hoodieTableSource = new HoodieTableSource(
                tableSchema,
                new Path(sourceTable.getTablePath()),
                Objects.isNull(conf.getString(FlinkOptions.PARTITION_PATH_FIELD)) ? null :
                        Arrays.asList(conf.getString(FlinkOptions.PARTITION_PATH_FIELD)),
                DEFAULT_PARTNAME,
                conf);

        // 3.register source table
        Table table = tableEnv.fromTableSource(hoodieTableSource);

        // Note: flink 1.12.x TableEnvironment need to TableEnvironmentInternal can work
        ((TableEnvironmentInternal)tableEnv).registerTableSourceInternal(sourceTable.getName(), hoodieTableSource);

        // debug
        table.printSchema();
    }

    // Note: this method by flink 1.11.x
    /*private static void registeSink(TableEnvironment tableEnv, SinkTable sinkTable, TableSchema sinkTableSchema) {
        Configuration conf = sinkTable.getConfiguration();

        // auto init hudi storage
        if (sinkTable.isInitTableIfNotExists()) {
            String avroSchema = AvroSchemaConverter.convertToSchema(
                    sinkTableSchema.toRowDataType().notNull().getLogicalType()).toString();
            conf.setString(FlinkOptions.READ_AVRO_SCHEMA, avroSchema);

            try {
                StreamerUtil.initTableIfNotExists(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // instant HoodieTableSink
        HoodieTableSink hoodieTableSink = new HoodieTableSink(conf, sinkTableSchema);

        // registe sink table
        tableEnv.registerTableSink(sinkTable.getName(), hoodieTableSink);
    }*/

    // Note: this method by flink 1.12.x
    private static void registeSink(TableEnvironment tableEnv, SinkTable sinkTable, TableSchema sinkTableSchema) {
        Configuration conf = sinkTable.getConfiguration();

        // auto init hudi storage
        if (sinkTable.isInitTableIfNotExists()) {
            String avroSchema = AvroSchemaConverter.convertToSchema(
                    sinkTableSchema.toRowDataType().notNull().getLogicalType()).toString();
            conf.setString(FlinkOptions.READ_AVRO_SCHEMA, avroSchema);

            try {
                StreamerUtil.initTableIfNotExists(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // instant HoodieTableSink
        HoodieTableSink hoodieTableSink = new HoodieTableSink(conf, sinkTableSchema);

        // registe sink table
        ((TableEnvironmentInternal)tableEnv).registerTableSinkInternal(sinkTable.getName(), hoodieTableSink);
    }

    public static void run(TableEnvironment tableEnv, ApplicationConfig config) {
        // 1. registe source table
        config.getSource().getTable().stream().forEach(t -> registeSource(tableEnv, t));

        // 2 split script
        String[] statements = split(config.getTransform());

        Map<String, SinkTable> sinkTableMap = config.getSink().getTable().stream()
                .collect(Collectors.toMap(SinkTable::getName, sinkTable -> sinkTable));

        Arrays.stream(statements).forEach(sql -> {
            // process insert script
            String command = StringUtils.strip(StringUtils.trim(sql), SQL_EOF);
            SqlNode sqlNode = FlinkSqlParser.parseSqlNode(command);
            // insert
            if (sqlNode instanceof RichSqlInsert) {
                RichSqlInsert insertSqlNode = (RichSqlInsert) sqlNode;
                String targetTable = insertSqlNode.getTargetTable().toString();
                String sourceCommand = insertSqlNode.getSource().toString();
                Table insertTable = tableEnv.sqlQuery(sourceCommand);
                // 3. registe sink table
                registeSink(tableEnv, sinkTableMap.get(targetTable), insertTable.getSchema());

            } else if (sqlNode instanceof SqlSelect) { // select

            }

            // 4.execute command
            tableEnv.executeSql(command);
        });
    }

    private static String[] split(String script) {
        return script.split(SCRIPT_SPLIT_CHAR);
    }
}
