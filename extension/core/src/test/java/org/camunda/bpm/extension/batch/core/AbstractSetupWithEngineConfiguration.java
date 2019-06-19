package org.camunda.bpm.extension.batch.core;

import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.mockito.Mock;

import java.nio.charset.Charset;

import static org.mockito.Mockito.when;

public class AbstractSetupWithEngineConfiguration {

  @Mock
  protected ProcessEngineConfigurationImpl engineConfiguration;

  @Mock
  protected CommandContext commandContext;

  @Mock
  protected ProcessEngineImpl processEngine;

  public void setUp() {
    when(engineConfiguration.getProcessEngine()).thenReturn(processEngine);
    when(processEngine.getProcessEngineConfiguration()).thenReturn(engineConfiguration);
    when(engineConfiguration.getDefaultCharset()).thenReturn(Charset.defaultCharset());
    Context.setProcessEngineConfiguration(engineConfiguration);
    Context.setCommandContext(commandContext);
  }

}
