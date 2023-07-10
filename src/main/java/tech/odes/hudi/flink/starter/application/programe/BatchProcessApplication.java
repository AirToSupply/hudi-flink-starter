package tech.odes.hudi.flink.starter.application.programe;

import tech.odes.hudi.flink.starter.common.config.ApplicationConfig;
import tech.odes.hudi.flink.starter.common.env.BatchContext;
import tech.odes.hudi.flink.starter.common.env.ExecMode;
import tech.odes.hudi.flink.starter.common.env.ExectionContextFactory;
import tech.odes.hudi.flink.starter.common.service.ProceessRunner;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableEnvironment;
import org.yaml.snakeyaml.Yaml;

/**
 * batch operate hudi skeleton with Flink SQL
 */
public class BatchProcessApplication {

    private static final String CONFIG_FILE_NAME = "transform-batch.yaml";

    public static void main(String[] args) {

        BatchContext batchContext = (BatchContext) ExectionContextFactory.getExectionContext(ExecMode.BATCH);

        StreamExecutionEnvironment execEnv = batchContext.getStreamExecutionEnvironment();
        TableEnvironment tableEnv = batchContext.getTableEnvironment();

        ApplicationConfig applicationConfig = new Yaml().loadAs(BatchProcessApplication.class
                .getClassLoader().getResourceAsStream(CONFIG_FILE_NAME), ApplicationConfig.class);

        ProceessRunner.run(tableEnv, applicationConfig);
    }
}
