package tech.odes.hudi.flink.starter.common.parser;

import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.flink.sql.parser.impl.FlinkSqlParserImpl;
import org.apache.flink.sql.parser.validate.FlinkSqlConformance;

/**
 * flink sql parser
 */
public class FlinkSqlParser {

    private static final SqlParser.Config CONFIG = SqlParser.configBuilder()
            .setParserFactory(FlinkSqlParserImpl.FACTORY)
            .setConformance(FlinkSqlConformance.DEFAULT)
            .setQuotedCasing(Casing.UNCHANGED)
            .setUnquotedCasing(Casing.UNCHANGED)
            .setQuoting(Quoting.BACK_TICK)
            .setCaseSensitive(true)
            .setIdentifierMaxLength(1024)
            .build();

    public static SqlNode parseSqlNode(String sql) {
        SqlParser parser = SqlParser.create(sql, CONFIG);
        try {
            return parser.parseStmt();
        } catch (SqlParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
