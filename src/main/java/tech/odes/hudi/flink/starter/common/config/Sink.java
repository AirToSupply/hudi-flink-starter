package tech.odes.hudi.flink.starter.common.config;

import java.util.List;
import java.util.Objects;

public class Sink {
    private String basePath;

    private List<SinkTable> table;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public List<SinkTable> getTable() {
        return table;
    }

    public void setTable(List<SinkTable> table) {
        this.table = table;
        table.stream().forEach(t -> checkBasePath(t));
    }

    private void checkBasePath(SinkTable sinkTable) {
        if (Objects.isNull(sinkTable.getBasePath())) {
            sinkTable.setBasePath(this.getBasePath());
        }
    }
}
