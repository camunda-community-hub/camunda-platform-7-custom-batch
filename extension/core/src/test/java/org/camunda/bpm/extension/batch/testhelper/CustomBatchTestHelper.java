package org.camunda.bpm.extension.batch.testhelper;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.managementService;

import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.batch.BatchMonitorJobHandler;
import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.runtime.Job;

public class CustomBatchTestHelper {

  public static JobDefinition getSeedJobDefinition(Batch batch) {
    return managementService().createJobDefinitionQuery()
      .jobDefinitionId(batch.getSeedJobDefinitionId())
      .jobType(BatchSeedJobHandler.TYPE)
      .singleResult();
  }

  public static JobDefinition getMonitorJobDefinition(Batch batch) {
    return managementService().createJobDefinitionQuery()
      .jobDefinitionId(batch.getMonitorJobDefinitionId())
      .jobType(BatchMonitorJobHandler.TYPE)
      .singleResult();
  }

  public static Job getSeedJob(Batch batch) {
    return getJobForDefinition(getSeedJobDefinition(batch));
  }

  public static Job getMonitorJob(Batch batch) {
    return getJobForDefinition(getMonitorJobDefinition(batch));
  }

  public static Job getJobForDefinition(JobDefinition jobDefinition) {
    if (jobDefinition != null) {
      return managementService().createJobQuery()
        .jobDefinitionId(jobDefinition.getId())
        .singleResult();
    } else {
      return null;
    }
  }

  public static List<Job> getJobsForDefinition(JobDefinition jobDefinition) {
    return managementService().createJobQuery()
      .jobDefinitionId(jobDefinition.getId())
      .list();
  }

  public static JobDefinition getGeneratorJobDefinition(Batch batch) {
    return managementService().createJobDefinitionQuery()
      .jobDefinitionId(batch.getBatchJobDefinitionId())
      .jobType(batch.getType())
      .singleResult();
  }

  public static void executeJob(String jobId) {
    managementService().executeJob(jobId);
  }

}
