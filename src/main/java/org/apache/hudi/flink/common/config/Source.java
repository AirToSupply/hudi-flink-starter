package org.apache.hudi.flink.common.config;

import java.util.List;
import java.util.Objects;

public class Source {
    private String basePath;

    private List<SourceTable> table;

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public List<SourceTable> getTable() {
        return table;
    }

    public void setTable(List<SourceTable> table) {
        this.table = table;
        table.stream().forEach(t -> checkBasePath(t));
    }

    private void checkBasePath(SourceTable sinkTable) {
        if (Objects.isNull(sinkTable.getBasePath())) {
            sinkTable.setBasePath(this.getBasePath());
        }
    }
}
