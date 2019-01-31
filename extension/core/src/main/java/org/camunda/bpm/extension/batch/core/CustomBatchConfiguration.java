package org.camunda.bpm.extension.batch.core;

import java.util.ArrayList;
import java.util.List;

public class CustomBatchConfiguration<T> {

  private final List<T> data;

  private final boolean exclusive;

  public static <T> CustomBatchConfiguration<T> of(final List<T> data, final boolean exclusive) {
    return new CustomBatchConfiguration<>(data, exclusive);
  }

  public static <T> CustomBatchConfiguration<T> of(final List<T> data) {
    return new CustomBatchConfiguration<>(data);
  }

  public CustomBatchConfiguration(final List<T> data) {
    this.data = new ArrayList<>(data);
    this.exclusive = true;
  }

  public CustomBatchConfiguration(final List<T> data, final boolean exclusive) {
    this.data = new ArrayList<>(data);
    this.exclusive = exclusive;
  }

  public List<T> getData() {
    return this.data;
  }

  public boolean isExclusive() {
    return exclusive;
  }
}
