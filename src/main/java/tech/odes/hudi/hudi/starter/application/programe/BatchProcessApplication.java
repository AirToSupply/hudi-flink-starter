package tech.odes.hudi.hudi.starter.application.programe;

import com.alibaba.fastjson.JSON;
import tech.odes.hudi.hudi.starter.common.config.ApplicationConfig;
import tech.odes.hudi.hudi.starter.common.env.BatchContext;
import tech.odes.hudi.hudi.starter.common.env.ExecMode;
import tech.odes.hudi.hudi.starter.common.env.ExectionContextFactory;
import tech.odes.hudi.hudi.starter.common.service.ProceessRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * batch operate hudi skeleton with Flink SQL
 */
public class BatchProcessApplication {

    private static Logger LOG = LoggerFactory.getLogger(BatchProcessApplication.class);

    private static final String OPTION_CONFIG = "config";

    public static void main(String[] args) {

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

        BatchContext batchContext = (BatchContext) ExectionContextFactory.getExectionContext(ExecMode.BATCH);
        StreamExecutionEnvironment execEnv = batchContext.getStreamExecutionEnvironment();
        TableEnvironment tableEnv = batchContext.getTableEnvironment();
        ProceessRunner.run(tableEnv, applicationConfig);
    }
}
