package org.camunda.bpm.extension.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.managementService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CustomBatchBuilderTest {

    @Rule
    public final ProcessEngineRule processEngineRule = new ProcessEngineRule();

    private TestCustomBatchJobHandler testCustomBatchJobHandler = new TestCustomBatchJobHandler();

    private ProcessEngineConfigurationImpl configuration;

    private List<String> data = Arrays.asList("Test", "Test2", "Test3", "Test4");

    @Before
    public void setUp() throws Exception {
        configuration = (ProcessEngineConfigurationImpl) processEngine().getProcessEngineConfiguration();
        configuration.setCustomBatchJobHandlers(new ArrayList<>());
        configuration.getCustomBatchJobHandlers().add(testCustomBatchJobHandler);
    }

    @Test
    public void create_batch_with_defaults() throws Exception {
        final Batch batch = CustomBatchBuilder.of(configuration)
            .jobHandler(testCustomBatchJobHandler)
            .batchData(data)
            .create(configuration.getCommandExecutorTxRequired());

        assertThat(batch).isNotNull();
        assertThat(batch.getType()).isEqualTo("test-type");
        assertThat(batch.getBatchJobsPerSeed()).isEqualTo(100); //Default camunda value
        assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(1); //Default camunda value

        final Batch batchLookup = managementService().createBatchQuery().singleResult();
        assertThat(batchLookup).isNotNull();

        //Cleanup
        managementService().deleteBatch(batchLookup.getId(), true);
    }

    @Test
    public void create_batch_with_own_config() throws Exception {
        final Batch batch = CustomBatchBuilder.of(configuration)
            .jobHandler(testCustomBatchJobHandler)
            .jobsPerSeed(10)
            .invocationsPerBatchJob(5)
            .batchData(data)
            .create(configuration.getCommandExecutorTxRequired());

        assertThat(batch).isNotNull();
        assertThat(batch.getType()).isEqualTo("test-type");
        assertThat(batch.getBatchJobsPerSeed()).isEqualTo(10);
        assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(5);

        final Batch batchLookup = managementService().createBatchQuery().singleResult();
        assertThat(batchLookup).isNotNull();

        //Cleanup
        managementService().deleteBatch(batchLookup.getId(), true);
    }

    @Test
    public void seed_job_is_created()
    {
        final Batch batch = CustomBatchBuilder.of(configuration)
            .jobHandler(testCustomBatchJobHandler)
            .batchData(data)
            .create(configuration.getCommandExecutorTxRequired());

        JobDefinition seedJobDefinition = getSeedJobDefinition(batch);
        assertThat(seedJobDefinition).isNotNull();

        Job seedJob = getSeedJob(batch);
        assertThat(seedJob).isNotNull();

        //Cleanup
        managementService().deleteBatch(batch.getId(), true);
    }

    @Test
    public void batch_jobs_are_created()
    {
        final Batch batch = CustomBatchBuilder.of(configuration)
            .jobHandler(testCustomBatchJobHandler)
            .jobsPerSeed(4)
            .invocationsPerBatchJob(2)
            .batchData(data)
            .create(configuration.getCommandExecutorTxRequired());

        final Job seedJob = getSeedJob(batch);
        executeJob(seedJob.getId());

        final List<Job> list = getJobsForDefinition(getGeneratorJobDefinition(batch));
        assertThat(list.size()).isEqualTo(2);

        //Cleanup
        managementService().deleteBatch(batch.getId(), true);
    }

    private JobDefinition getSeedJobDefinition(Batch batch) {
        return managementService().createJobDefinitionQuery()
            .jobDefinitionId(batch.getSeedJobDefinitionId())
            .jobType(BatchSeedJobHandler.TYPE)
            .singleResult();
    }

    private Job getSeedJob(Batch batch) {
        return getJobForDefinition(getSeedJobDefinition(batch));
    }

    private Job getJobForDefinition(JobDefinition jobDefinition) {
        if (jobDefinition != null) {
            return managementService().createJobQuery()
                .jobDefinitionId(jobDefinition.getId())
                .singleResult();
        }
        else {
            return null;
        }
    }

    private List<Job> getJobsForDefinition(JobDefinition jobDefinition) {
        return managementService().createJobQuery()
            .jobDefinitionId(jobDefinition.getId())
            .list();
    }

    private JobDefinition getGeneratorJobDefinition(Batch batch) {
        return managementService().createJobDefinitionQuery()
            .jobDefinitionId(batch.getBatchJobDefinitionId())
            .jobType(batch.getType())
            .singleResult();
    }

    private void executeJob(String jobId) {
        managementService().executeJob(jobId);
    }
}
