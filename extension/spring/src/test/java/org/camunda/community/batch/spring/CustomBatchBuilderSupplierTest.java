package org.camunda.community.batch.spring;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.community.batch.CustomBatchBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CustomBatchBuilderSupplierTest.TestConfig.class)
public class CustomBatchBuilderSupplierTest {

  @Import(CustomBatchBuilderSupplier.class)
  static class TestConfig {

    @Bean
    public ProcessEngineConfiguration processEngineConfiguration() {
      return mock(ProcessEngineConfigurationImpl.class);
    }

  }

  @Autowired
  private CustomBatchBuilderSupplier<String> stringCustomBatchBuilderSupplier;

  @Test
  public void supply_for_string() {
    assertThat(stringCustomBatchBuilderSupplier).isNotNull();
    CustomBatchBuilder<String> batchBuilder = stringCustomBatchBuilderSupplier.get();
    assertThat(batchBuilder).isNotNull();
  }
}
