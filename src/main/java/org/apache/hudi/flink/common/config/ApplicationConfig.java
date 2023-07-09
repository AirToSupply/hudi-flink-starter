package org.apache.hudi.flink.common.config;

public class ApplicationConfig {
    private Source source;

    private Sink sink;

    private String transform;

    public ApplicationConfig() {
    }

    public ApplicationConfig(Source source, Sink sink, String transform) {
        this.source = source;
        this.sink = sink;
        this.transform = transform;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Sink getSink() {
        return sink;
    }

    public void setSink(Sink sink) {
        this.sink = sink;
    }

    public String getTransform() {
        return transform;
    }

    public void setTransform(String transform) {
        this.transform = transform;
    }
}
