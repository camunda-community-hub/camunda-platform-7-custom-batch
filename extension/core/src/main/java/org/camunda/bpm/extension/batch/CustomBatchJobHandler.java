package org.camunda.bpm.extension.batch;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchJobConfiguration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.extension.batch.core.CustomBatchConfiguration;
import org.camunda.bpm.extension.batch.core.CustomBatchCreateJobsHandler;

public abstract class CustomBatchJobHandler<T> extends CustomBatchCreateJobsHandler<T> {

  public abstract void execute(List<T> data, CommandContext commandContext);

  @Override
  public void execute(BatchJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {
    final ByteArrayEntity configurationEntity = commandContext
      .getDbEntityManager()
      .selectById(ByteArrayEntity.class, configuration.getConfigurationByteArrayId());
    final CustomBatchConfiguration<T> jobConfiguration = readConfiguration(configurationEntity.getBytes());

    execute(jobConfiguration.getData(), commandContext);
  }

  @Override
  public void onDelete(BatchJobConfiguration configuration, JobEntity jobEntity) {
    String byteArrayId = configuration.getConfigurationByteArrayId();
    if (byteArrayId != null) {
      Context.getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(byteArrayId);
    }
  }
}
