package tech.odes.hudi.hudi.starter.common.env;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExectionContextFactory {

    private static Logger LOG = LoggerFactory.getLogger(ExectionContextFactory.class);

    public static ExectionContext getExectionContext(ExecMode mode) {
        StreamExecutionEnvironment execEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        switch (mode) {
            case BATCH:
                return new BatchContext(
                        execEnv,
                        TableEnvironment.create(
                                EnvironmentSettings.newInstance().useBlinkPlanner().inBatchMode().build()));
            case STREAM:
                return new StreamContext(
                        execEnv,
                        StreamTableEnvironment.create(execEnv,
                                EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build()));
            default:
                LOG.error("ExectionContext must be initialized !");
                throw new RuntimeException("Exection Mode must be requires!");
        }
    }



}
