package org.camunda.bpm.extension.batch.core;

import java.util.ArrayList;
import java.util.List;

public class CustomBatchConfiguration<T> {
    private List<T> data;

    public CustomBatchConfiguration(final List<T> data) { this.data = new ArrayList<T>(data); }

    public List<T> getData() { return this.data; }
}
