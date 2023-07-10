package tech.odes.hudi.hudi.starter.common.schema;

import tech.odes.hudi.hudi.starter.common.config.HoodieTableConnector;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.TableSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HoodieTableRegistry {

    private static Logger LOG = LoggerFactory.getLogger(HoodieTableRegistry.class);

    public static void connect(TableEnvironment tableEnv, HoodieTableConnector tableConnector) {

        TableSchema tableSchema = HoodieTableSchemaHelper.getTableSchemaByTablePath(tableConnector.getTablePath());

        // hudi source table code gen
        String connectSourceTableDDL = HoodieTableCodeGenerator.codeGen(tableConnector, tableSchema);

        LOG.info("-------------------------------------------------");
        LOG.info("register hudi table [{}]: \n {}", tableConnector.getName(), connectSourceTableDDL);
        LOG.info("-------------------------------------------------");

        tableEnv.executeSql(connectSourceTableDDL);

        LOG.info("register hudi table [{}] success!", tableConnector.getName());
    }
}
