package tech.odes.hudi.flink.starter.common.schema;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableSchema;
import org.apache.hudi.configuration.FlinkOptions;
import tech.odes.hudi.flink.starter.common.config.HoodieTableConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Code Generator DDL about Flink SQL
 */
public class HoodieTableCodeGenerator {

    private static Logger LOG = LoggerFactory.getLogger(HoodieTableCodeGenerator.class);

    public static String codeGen(HoodieTableConnector tableConnector, TableSchema schema) {

        // create table
        String createTable = "create table " + tableConnector.getName() + " (\n";
        StringBuilder builder = new StringBuilder(createTable);

        // table columns
        List<TableColumn> tableColumns = schema.getTableColumns();
        if (Objects.isNull(tableColumns) || tableColumns.size() == 0) {
            LOG.error("Table [{}] has no columns!", tableConnector.getName());
            throw new RuntimeException("There are no columns in the table [" + tableConnector.getName() + "]");
        }

        for (int index = 0; index < tableColumns.size(); index++) {
            TableColumn tableColumn = tableColumns.get(index);
            builder.append("  `").append(tableColumn.getName()).append("` ")
                   .append(tableColumn.getType().getLogicalType().asSummaryString());
            if (index != tableColumns.size() - 1) {
                builder.append(",").append("\n");
            }
        }
        builder.append("\n)");

        Map<String, String> options = tableConnector.getConfiguration().toMap();

        // partition columns
        String paritionKeys = options.get(FlinkOptions.PARTITION_PATH_FIELD.key());
        if (Objects.nonNull(paritionKeys)) {
            // note: Here, we first ignore the partition columns existence judgment,
            //       and let the exception be handled by the Flink execution layer.
            List<String> correctPartitionKeys = Arrays.stream(paritionKeys.split(","))
                    .filter(field -> StringUtils.isNotEmpty(field)).collect(Collectors.toList());

            if (correctPartitionKeys.size() != 0) {
                builder.append(" PARTITIONED BY (");
                builder.append(
                        StringUtils.join(
                                correctPartitionKeys.stream()
                                        .map(f -> String.format("`%s`", f))
                                        .collect(Collectors.toList()),
                                ","));
                builder.append(")\n");
            }
        }

        // table options
        builder.append("with (\n  'connector' = 'hudi'");
        if (options.size() != 0) {
            options.forEach((k, v) -> builder.append(",\n")
                    .append("  '").append(k).append("' = '").append(v).append("'"));
        }
        builder.append("\n)");

        return builder.toString();
    }

}
