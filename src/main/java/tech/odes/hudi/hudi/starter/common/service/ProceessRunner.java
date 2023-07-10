package tech.odes.hudi.hudi.starter.common.service;

import tech.odes.hudi.hudi.starter.common.config.ApplicationConfig;
import tech.odes.hudi.hudi.starter.common.config.SourceTable;
import tech.odes.hudi.hudi.starter.common.parser.FlinkSqlParser;
import tech.odes.hudi.hudi.starter.common.schema.HoodieTableCodeGenerator;
import tech.odes.hudi.hudi.starter.common.schema.HoodieTableSchemaHelper;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.sql.parser.dml.RichSqlInsert;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.apache.hudi.configuration.FlinkOptions;
import tech.odes.hudi.hudi.starter.common.config.SinkTable;
import org.apache.hudi.util.AvroSchemaConverter;
import org.apache.hudi.util.StreamerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ProceessRunner {

    private static Logger LOG = LoggerFactory.getLogger(ProceessRunner.class);

    private static String SCRIPT_SPLIT_CHAR = ";\\n";

    private static String SQL_EOF = ";";

    private static void registeSource(TableEnvironment tableEnv, SourceTable sourceTable) {
        // obtain hudi table schema by storage location
        TableSchema tableSchema = HoodieTableSchemaHelper.getTableSchemaByTablePath(sourceTable.getTablePath());

        // hudi source table code gen
        String connectSourceTableDDL = HoodieTableCodeGenerator.codeGen(sourceTable, tableSchema);

        LOG.info("-------------------------------------------------");
        LOG.info("register hudi source table [{}]: \n {}", sourceTable.getName(), connectSourceTableDDL);
        LOG.info("-------------------------------------------------");

        tableEnv.executeSql(connectSourceTableDDL);

        LOG.info("register hudi source table [{}] success!", sourceTable.getName());
    }

    private static void registeSink(TableEnvironment tableEnv, SinkTable sinkTable, TableSchema sinkTableSchema) {
        Configuration conf = sinkTable.getConfiguration();

        // auto init hudi storage
        if (sinkTable.isInitTableIfNotExists()) {
            String avroSchema = AvroSchemaConverter.convertToSchema(
                    sinkTableSchema.toRowDataType().notNull().getLogicalType()).toString();
            conf.setString(FlinkOptions.SOURCE_AVRO_SCHEMA, avroSchema);

            try {
                StreamerUtil.initTableIfNotExists(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // hudi sink table code gen
        String connectSinkTableDDL = HoodieTableCodeGenerator.codeGen(sinkTable, sinkTableSchema);

        LOG.info("-------------------------------------------------");
        LOG.info("register hudi sink table [{}]: \n {}", sinkTable.getName(), connectSinkTableDDL);
        LOG.info("-------------------------------------------------");

        tableEnv.executeSql(connectSinkTableDDL);

        LOG.info("register hudi sink table [{}] success!", sinkTable.getName());
    }

    public static void run(TableEnvironment tableEnv, ApplicationConfig config) {
        // 1. registe source tables
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
                LOG.info("--> [insert] source command: {} | target table: {}", sourceCommand, targetTable);
                Table insertTable = tableEnv.sqlQuery(sourceCommand);
                // 3. registe sink table
                registeSink(tableEnv, sinkTableMap.get(targetTable), insertTable.getSchema());

            } else if (sqlNode instanceof SqlSelect) {
                // select
            }

            // 4.execute command
            LOG.info("--> SQL: {} | SQL Node Type: {}", command, sqlNode.getClass().getName());
            tableEnv.executeSql(command);
        });
    }

    private static String[] split(String script) {
        return script.split(SCRIPT_SPLIT_CHAR);
    }
}
