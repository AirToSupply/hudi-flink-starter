package org.apache.hudi.flink.application.programe;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.hudi.flink.common.config.ApplicationConfig;
import org.apache.hudi.flink.common.env.BatchContext;
import org.apache.hudi.flink.common.env.ExecMode;
import org.apache.hudi.flink.common.env.ExectionContextFactory;
import org.apache.hudi.flink.common.service.ProceessRunner;
import org.yaml.snakeyaml.Yaml;

public class BatchProcessApplication {

    public static void main(String[] args) {

        BatchContext batchContext = (BatchContext) ExectionContextFactory.getExectionContext(ExecMode.BATCH);

        StreamExecutionEnvironment execEnv = batchContext.getStreamExecutionEnvironment();
        TableEnvironment tableEnv = batchContext.getTableEnvironment();

        ApplicationConfig applicationConfig = new Yaml().loadAs(BatchProcessApplication.class
                .getClassLoader().getResourceAsStream("transform.yaml"), ApplicationConfig.class);

        ProceessRunner.run(tableEnv, applicationConfig);
    }
}
