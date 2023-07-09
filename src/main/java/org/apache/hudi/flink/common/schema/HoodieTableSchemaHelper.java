package org.apache.hudi.flink.common.schema;

import org.apache.avro.Schema;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.table.api.TableColumn;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.types.DataType;
import org.apache.hadoop.conf.Configuration;
import org.apache.hudi.common.table.HoodieTableMetaClient;
import org.apache.hudi.common.table.TableSchemaResolver;
import org.apache.hudi.exception.TableNotFoundException;
import org.apache.hudi.util.AvroSchemaConverter;
import org.apache.hudi.util.StreamerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HoodieTableSchemaHelper {

    private static Logger LOG = LoggerFactory.getLogger(HoodieTableSchemaHelper.class);

    private static final String DELIMITER = "/";

    public static String getCreateHoodieTableDDL(HoodieTableDescriptor hoodieTableDescriptor, Map<String, String> options) {
        String createTable = "create table " + hoodieTableDescriptor.getTableName() + "(\n";
        StringBuilder builder = new StringBuilder(createTable);
        // table column
        //
        builder.append("\n)");
        if (options.size() != 0) {
            options.forEach((k, v) -> builder.append(",\n")
                    .append("  '").append(k).append("' = '").append(v).append("'"));
        }
        builder.append("\n)");
        return builder.toString();
    }

    /**
     * obtain hudi table schema by basePath, namespacea and tableName
     *
     * @param basePath
     *          hudi table storage location base path
     *
     * @param namespace
     *          hudi table namespace
     *
     * @param tableName
     *          hudi table name
     *
     * @return
     *
     * @throws Exception
     */
    public static TableSchema getTableSchemaByTableName(
            String basePath, String namespace, String tableName) throws Exception {
        TableSchema schema = null;
        try {
            schema = getTableSchemaByTablePath(String.join(DELIMITER, basePath, namespace, tableName));
        } catch (TableNotFoundException e) {
            LOG.error("Hoodie table storage [{}] not found", tableName);
        }
        return schema;
    }

    public static String getTablePath(String basePath, String namespace, String tableName) throws Exception {
        return String.join(DELIMITER, basePath, namespace, tableName);
    }

    /**
     * obtain hudi table schema by storage location
     *
     *
     * @param tablePath
     *          hudi table storage location
     *
     * @return
     *          hudi table schema
     *
     * @throws Exception
     */
    public static TableSchema getTableSchemaByTablePath(String tablePath) {
        TableSchema schema = null;

        try {
            Configuration hadoopConf = StreamerUtil.getHadoopConf();

            HoodieTableMetaClient metaClient = HoodieTableMetaClient.builder()
                    .setConf(hadoopConf).setBasePath(tablePath).build();

            TableSchemaResolver resolver = new TableSchemaResolver(metaClient);
            Schema tableAvroSchema = resolver.getTableAvroSchema(false);

            List<Tuple2<String, DataType>> fieldMetas = tableAvroSchema.getFields().stream()
                    .map(field -> Tuple2.of(field.name(), AvroSchemaConverter.convertToDataType(field.schema())))
                    .collect(Collectors.toList());

            String[] names = new String[fieldMetas.size()];
            DataType[] dataTypes = new DataType[fieldMetas.size()];

            fieldMetas.stream().map(tuple -> tuple.f0).collect(Collectors.toList()).toArray(names);
            fieldMetas.stream().map(tuple -> tuple.f1).collect(Collectors.toList()).toArray(dataTypes);

            schema = TableSchema.builder().fields(names, dataTypes).build();

        } catch (TableNotFoundException e) {
            LOG.error("Hoodie table storage [{}] not found", tablePath);
        } catch (Exception ex) {
            LOG.error("parse table schema error!", ex);
        }
        return schema;
    }
}
