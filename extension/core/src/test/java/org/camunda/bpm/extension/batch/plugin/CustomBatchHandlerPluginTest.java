package org.camunda.bpm.extension.batch.plugin;

import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CustomBatchHandlerPluginTest {

  @Mock
  BatchJobHandler<String> jobHandler;

  @Mock
  BatchJobHandler<Long> jobHandler2;

  ProcessEngineConfigurationImpl configuration = new StandaloneInMemProcessEngineConfiguration();

  @Test
  public void createPluginFromList() {
    final CustomBatchHandlerPlugin plugin = CustomBatchHandlerPlugin.of(Arrays.asList(jobHandler, jobHandler2));

    plugin.preInit(configuration);

    assertThat(configuration.getCustomBatchJobHandlers()).hasSize(2);
  }

  @Test
  public void createPluginWithSingleJobHandler() {
    final CustomBatchHandlerPlugin plugin = CustomBatchHandlerPlugin.of(jobHandler);

    plugin.preInit(configuration);

    assertThat(configuration.getCustomBatchJobHandlers()).hasSize(1);
  }

  @Test
  public void createPluginWhichDoesNothing() {
    final CustomBatchHandlerPlugin plugin = CustomBatchHandlerPlugin.deactivate();

    plugin.preInit(configuration);

    assertThat(configuration.getCustomBatchJobHandlers()).hasSize(0);
  }

}
