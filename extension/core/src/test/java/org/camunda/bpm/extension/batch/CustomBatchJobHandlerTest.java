package org.camunda.bpm.extension.batch;

import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.extension.batch.core.CustomBatchConfiguration;
import org.camunda.bpm.extension.batch.core.CustomBatchConfigurationHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomBatchJobHandlerTest {

  @Mock
  private DbEntityManager dbEntityManager;

  @Mock
  private ByteArrayManager byteArrayManager;

  @Mock
  private CustomBatchConfigurationHelper<String> configurationHelper;

  @Mock
  private CommandContext commandContext;

  @Spy
  private TestCustomBatchJobHandler jobHandler;

  private final BatchJobConfiguration batchJobConfiguration = new BatchJobConfiguration("1");

  @Test
  public void configurationGetsLoadedFromByteArray() {
    // GIVEN
    final ByteArrayEntity byteArrayEntity = new ByteArrayEntity("someName", "someBytes".getBytes());
    when(commandContext.getDbEntityManager()).thenReturn(dbEntityManager);
    when(dbEntityManager.selectById(ByteArrayEntity.class, batchJobConfiguration.getConfigurationByteArrayId()))
      .thenReturn(byteArrayEntity);

    when(configurationHelper.readConfiguration(byteArrayEntity.getBytes()))
      .thenReturn(CustomBatchConfiguration.of(Arrays.asList("bla", "blu")));

    // WHEN engine calls abstract execute method
    jobHandler.execute(batchJobConfiguration, null, commandContext, null);

    // THEN job config should be loaded form byte array
    verify(configurationHelper).readConfiguration(byteArrayEntity.getBytes());

    // AND execute method should get list of data
    verify(jobHandler).execute(anyList(), eq(commandContext));
  }

  @Test
  public void batchConfigurationGetsDeleted() {
    // GIVEN
    when(commandContext.getByteArrayManager()).thenReturn(byteArrayManager);
    Context.setCommandContext(commandContext);

    // WHEN engine calls onDelete
    jobHandler.onDelete(batchJobConfiguration, null);

    // THEN batch config gets deleted
    verify(byteArrayManager).deleteByteArrayById(batchJobConfiguration.getConfigurationByteArrayId());
  }

  class TestCustomBatchJobHandler extends CustomBatchJobHandler<String> {

    @Override
    public void execute(final List<String> data, final CommandContext commandContext) { }

    @Override
    public String getType() {
      return "test-type";
    }

    @Override
    public CustomBatchConfigurationHelper<String> configurationHelper() {
      return configurationHelper;
    }
  }
}
