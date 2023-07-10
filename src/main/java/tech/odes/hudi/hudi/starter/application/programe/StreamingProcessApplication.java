package tech.odes.hudi.hudi.starter.application.programe;

import com.alibaba.fastjson.JSON;
import tech.odes.hudi.hudi.starter.common.config.ApplicationConfig;
import tech.odes.hudi.hudi.starter.common.env.ExecMode;
import tech.odes.hudi.hudi.starter.common.env.ExectionContextFactory;
import tech.odes.hudi.hudi.starter.common.env.StreamContext;
import tech.odes.hudi.hudi.starter.common.service.ProceessRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * streaming operate hudi skeleton with Flink SQL
 */
public class StreamingProcessApplication {

    private static Logger LOG = LoggerFactory.getLogger(StreamingProcessApplication.class);

    private static final String OPTION_CONFIG = "config";

    private static final String CONFIG_FILE_NAME = "transform-streaming.yaml";

    private static final long CHECKPOINTING_INTERVAL = 1000 * 5L;

    public static void main(String[] args) throws Exception {

        ParameterTool parameters = ParameterTool.fromArgs(args);

        String config = parameters.get(OPTION_CONFIG, null);

        if (StringUtils.isBlank(config)) {
            throw new RuntimeException("arguments [" + OPTION_CONFIG + "] must be setting!");
        }

        ApplicationConfig applicationConfig = null;

        try {
            LOG.info("arguments [" + OPTION_CONFIG + "] : {}", config);
            applicationConfig = JSON.parseObject(config, ApplicationConfig.class);
        } catch (Exception ex) {
            throw new RuntimeException("arguments [" + OPTION_CONFIG + "] error!");
        }

        StreamContext exectionContext = (StreamContext) ExectionContextFactory.getExectionContext(ExecMode.STREAM);

        StreamExecutionEnvironment execEnv = exectionContext.getStreamExecutionEnvironment();
        // Note: must be checkpoint
        execEnv.enableCheckpointing(CHECKPOINTING_INTERVAL);
        execEnv.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        StreamTableEnvironment tableEnv = exectionContext.getStreamTableEnvironment();
        ProceessRunner.run(tableEnv, applicationConfig);
    }
}