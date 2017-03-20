package org.camunda.bpm.extension.batch.testhelper;

import java.io.Serializable;

public class TestConfigObject implements Serializable {
  public String text;
  public Integer something = 1;

  public TestConfigObject(String text) {
    this.text = text;
  }
}
