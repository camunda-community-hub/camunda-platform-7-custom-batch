package org.camunda.bpm.extension.batch.core;

import java.io.Serializable;

public  class TestConfigObject implements Serializable {
    String text;
    Integer something = 1;

    TestConfigObject(String text) {
        this.text = text;
    }
}
