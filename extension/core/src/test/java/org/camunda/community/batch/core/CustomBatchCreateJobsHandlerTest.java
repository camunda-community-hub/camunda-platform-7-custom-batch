package org.camunda.community.batch.core;

import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobContext;
import org.camunda.bpm.engine.impl.batch.BatchJobDeclaration;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobDeclaration;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.community.batch.CustomBatchJobHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CustomBatchCreateJobsHandlerTest {

  private static final String BATCH_JOB_DEFINITION_ID = "1";

  @Mock
  private CustomBatchConfigurationHelper<String> configurationHelper;

  @Mock
  private BatchJobDeclaration batchJobDeclaration;

  @Mock
  private JobManager jobManager;

  private final TestCustomBatchJobHandler jobHandler = new TestCustomBatchJobHandler();

  @Captor
  private ArgumentCaptor<JobEntity> jobEntityArgumentCaptor;

  @Captor
  private ArgumentCaptor<CustomBatchConfiguration<String>> batchConfigurationArgumentCaptor;

  private final List<String> testData = Arrays.asList("bla", "blu");

  private final CustomBatchConfiguration<String> configuration = CustomBatchConfiguration.of(testData, false);

  @Before
  public void setUp() {
    when(configurationHelper.readConfiguration(any())).thenReturn(configuration);

    final CommandContext commandContextMock = mock(CommandContext.class);
    Context.setCommandContext(commandContextMock);
    when(commandContextMock.getJobManager()).thenReturn(jobManager);

    when(batchJobDeclaration.createJobInstance(any())).thenReturn(mock(MessageEntity.class));
  }

  @Test
  public void createJobsWithTwoSimpleSeeds() {
    final BatchEntity batchEntity = createBatchEntity(1,1);

    jobHandler.createJobs(batchEntity);
    assertThat(batchEntity.getJobsCreated()).isEqualTo(1);

    jobHandler.createJobs(batchEntity);
    assertThat(batchEntity.getJobsCreated()).isEqualTo(2);

    jobHandler.createJobs(batchEntity);
    assertThat(batchEntity.getJobsCreated()).isEqualTo(2);
    verify(jobManager, times(2)).insertAndHintJobExecutor(any());
  }

  @Test
  public void createJobsWithTwoPerSeedAndOneInvocationsPerJob() {
    final BatchEntity batchEntity = createBatchEntity(2, 1);

    jobHandler.createJobs(batchEntity);
    assertThat(batchEntity.getJobsCreated()).isEqualTo(2);

    jobHandler.createJobs(batchEntity);
    assertThat(batchEntity.getJobsCreated()).isEqualTo(2);

    verify(jobManager, times(2)).insertAndHintJobExecutor(any());
  }

  @Test
  public void createJobsWithOnePerSeedAndTwoInvocationsPerJob() {
    final BatchEntity batchEntity = createBatchEntity(1, 2);

    jobHandler.createJobs(batchEntity);
    assertThat(batchEntity.getJobsCreated()).isEqualTo(1);

    jobHandler.createJobs(batchEntity);
    assertThat(batchEntity.getJobsCreated()).isEqualTo(1);

    verify(jobManager).insertAndHintJobExecutor(any());
  }

  @Test
  public void createJobsExclusiveFlagIsSet() {
    final BatchEntity batchEntity = createBatchEntity(1,1);

    jobHandler.createJobs(batchEntity);
    verify(jobManager).insertAndHintJobExecutor(jobEntityArgumentCaptor.capture());

    assertThat(jobEntityArgumentCaptor.getValue()).isNotNull();
    assertThat(jobEntityArgumentCaptor.getValue().isExclusive()).isFalse();
  }

  @Test
  public void createJobsReturnsFalseIfMoreJobsNeedsToBeCreated() {
    final BatchEntity batchEntity = createBatchEntity(1,1);

    assertThat(jobHandler.createJobs(batchEntity)).isFalse();
  }

  @Test
  public void createJobsReturnsTrueIfNoMoreJobsNeedsToBeCreated() {
    final BatchEntity batchEntity = createBatchEntity(2,1);

    assertThat(jobHandler.createJobs(batchEntity)).isTrue();
  }

  @Test
  public void createJobsConfigurationPerJobIsSaved() {
    final BatchEntity batchEntity = createBatchEntity(1, 2);
    jobHandler.createJobs(batchEntity);

    verify(configurationHelper).saveConfiguration(batchConfigurationArgumentCaptor.capture());
    final CustomBatchConfiguration<String> argumentCaptorValue = batchConfigurationArgumentCaptor.getValue();
    assertThat(argumentCaptorValue).isNotNull();
    assertThat(argumentCaptorValue.getData()).isEqualTo(testData);
  }

  @Test
  public void deleteJobs() {
    final JobEntity jobEntity = mock(JobEntity.class);
    final JobEntity jobEntity2 = mock(JobEntity.class);
    final List<JobEntity> jobs = Arrays.asList(jobEntity, jobEntity2);
    when(jobManager.findJobsByJobDefinitionId(BATCH_JOB_DEFINITION_ID)).thenReturn(jobs);

    final BatchEntity batchEntity = createBatchEntity(1, 2);
    jobHandler.deleteJobs(batchEntity);

    verify(jobEntity).delete();
    verify(jobEntity2).delete();
  }

  @Test
  public void writeConfiguration() {
    jobHandler.writeConfiguration(configuration);

    verify(configurationHelper).writeConfiguration(configuration);
  }

  @Test
  public void readConfiguration() {
    final String configString = "some bytes";
    jobHandler.readConfiguration(configString.getBytes());

    verify(configurationHelper).readConfiguration(configString.getBytes());
  }

  private BatchEntity createBatchEntity(final int jobsPerSeed, final int invocationsPerJob) {
    final BatchEntity entity = new BatchEntity();
    entity.setBatchJobDefinitionId(BATCH_JOB_DEFINITION_ID);
    entity.setBatchJobsPerSeed(jobsPerSeed);
    entity.setInvocationsPerBatchJob(invocationsPerJob);
    return entity;
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

    @Override
    public JobDeclaration<BatchJobContext, MessageEntity> getJobDeclaration() {
      return batchJobDeclaration;
    }
  }
}
