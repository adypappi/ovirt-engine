package org.ovirt.engine.core.dao;

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.compat.Guid;

public class StepDaoTest extends BaseGenericDaoTestCase<Guid, Step, StepDao> {

    private static final Guid EXISTING_JOB_WITH_MULTIPLE_STEPS = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5889");
    private static final int TOTAL_STEPS_OF_MULTI_STEP_JOB = 8;
    private static final Guid EXISTING_JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5111");
    private static final Guid EXISTING_STEP_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509111111");
    private static final int TOTAL_STEPS = 13;
    private static final Guid EXISTING_STEP_WITH_SUB_STEPS = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5223");
    private static final int TOTAL_STEPS_OF_PARENT_STEP = 3;
    private static final Guid IN_PROGRESS_JOB_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509fb5333");
    private static final Guid IN_PROGRESS_STEP_ID = new Guid("54947df8-0e9e-4471-a2f9-9af509111333");
    private static final Guid IN_PROGRESS_REBALANCING_GLUSTER_VOLUME_TASK_ID = new Guid("44f714ed-2818-4350-b94a-8c3927e53f7c");
    private static final Guid REBALANCING_GLUSTER_VOLUME_STEP_ID = new Guid("cd75984e-1fd4-48fb-baf8-e45800a61a66");
    private static final int TOTAL_STEPS_OF_REBALANCING_GLUSTER_VOLUME = 1;

    @Inject
    private DiskDao diskDao;
    @Inject
    private StepSubjectEntityDao subjectEntityDao;
    @Inject
    private StepDao dao;

    @Override
    protected Guid getExistingEntityId() {
        return EXISTING_STEP_ID;
    }

    @Override
    protected StepDao prepareDao() {
        return dao;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return TOTAL_STEPS;
    }

    @Override
    protected Step generateNewEntity() {
        Step step = new Step(StepEnum.EXECUTING);
        step.setJobId(EXISTING_JOB_ID);
        step.setStepNumber(1);
        step.setDescription("Execution step");
        step.setCorrelationId("Some correlation id");
        return step;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setStatus(JobExecutionStatus.FINISHED);
        existingEntity.setEndTime(new Date());
        existingEntity.setProgress(50);
    }

    protected void verifyUpdate(Step existingEntity, Step result) {
        assertEquals("Progress should be equal", existingEntity.getProgress(), result.getProgress());
    }

    @Test
    public void existStep() {
        assertTrue(dao.exists(EXISTING_STEP_ID));
    }

    @Test
    public void nonExistStep() {
        assertFalse(dao.exists(Guid.newGuid()));
    }

    @Test
    public void getStepsByJobId() {
        List<Step> steps = dao.getStepsByJobId(EXISTING_JOB_WITH_MULTIPLE_STEPS);
        assertEquals("Verify Job has steps", TOTAL_STEPS_OF_MULTI_STEP_JOB, steps.size());
    }

    @Test
    public void getStepsByParentStepId() {
        List<Step> steps = dao.getStepsByParentStepId(EXISTING_STEP_WITH_SUB_STEPS);
        assertEquals("Verify Job has steps", TOTAL_STEPS_OF_PARENT_STEP, steps.size());
    }

    @Test
    public void updateJobStepsCompleted() {
        Step step = dao.get(IN_PROGRESS_STEP_ID);
        assertNotNull("Started step with ID " + IN_PROGRESS_STEP_ID, step);
        step.setStatus(JobExecutionStatus.FINISHED);
        Date endTime = new Date();
        step.setEndTime(endTime);
        dao.updateJobStepsCompleted(IN_PROGRESS_JOB_ID, JobExecutionStatus.FINISHED, endTime);
        Step afterUpdate = dao.get(IN_PROGRESS_STEP_ID);
        assertEquals("Compare step to itself after update in DB", step, afterUpdate);

    }

    @Test
    public void getStepsByExternalId(){
        List<Step> steps = dao.getStepsByExternalId(IN_PROGRESS_REBALANCING_GLUSTER_VOLUME_TASK_ID);
        assertEquals("Verify Rebalancing Gluster Volume Job has steps", TOTAL_STEPS_OF_REBALANCING_GLUSTER_VOLUME, steps.size());
        Step step = steps.get(0);
        assertEquals("Verify the Step Type status", StepEnum.REBALANCING_VOLUME, step.getStepType());
        assertEquals("Verify the Step status", JobExecutionStatus.STARTED, step.getStatus());
        assertEquals("Invalid Step", REBALANCING_GLUSTER_VOLUME_STEP_ID, step.getId());
    }

    @Test
    public void getExternalIdsForRunningSteps(){
        List<Guid> externalIds = dao.getExternalIdsForRunningSteps(ExternalSystemType.GLUSTER);
        assertEquals("Verify external ids present", 1, externalIds.size());
        assertEquals("Invalid TaskId", IN_PROGRESS_REBALANCING_GLUSTER_VOLUME_TASK_ID, externalIds.get(0));
    }

    @Test
    public void updateStepProgress(){
        Integer newProgress = 74;
        Step s = dao.get(FixturesTool.STEP_ID);
        assertNotEquals("New progress should be different than the current", newProgress, s.getProgress());
        updateStepProgress(FixturesTool.STEP_ID, newProgress);
        s = dao.get(FixturesTool.STEP_ID);
        assertEquals("New progress should be the same as the current", newProgress, s.getProgress());
    }

    private void prepareProgressTest(Guid entityId) {
        VdcObjectType type = VdcObjectType.Disk;

        BaseDisk diskImage = diskDao.get(entityId);
        assertProgress(null, diskImage);

        subjectEntityDao.saveAll(Arrays.asList(new StepSubjectEntity(FixturesTool.STEP_ID, type, entityId, 30),
                new StepSubjectEntity(FixturesTool.STEP_ID_2, type, entityId, 50)));
    }

    @Test
    public void diskStepProgress() {
        Guid entityId = FixturesTool.FLOATING_DISK_ID;
        prepareProgressTest(entityId);

        updateStepProgress(FixturesTool.STEP_ID, 10);
        updateStepProgress(FixturesTool.STEP_ID_2, 80);

        BaseDisk diskImage = diskDao.get(entityId);
        assertProgress(43, diskImage);
    }

    @Test
    public void diskNullStepProgress() {
        Guid entityId = FixturesTool.FLOATING_DISK_ID;
        prepareProgressTest(entityId);

        updateStepProgress(FixturesTool.STEP_ID, null);
        updateStepProgress(FixturesTool.STEP_ID_2, null);

        BaseDisk diskImage = diskDao.get(entityId);
        assertProgress(0, diskImage);
    }

    @Test
    public void diskNullFinishedStepProgress() {
        Guid entityId = FixturesTool.FLOATING_DISK_ID;
        prepareProgressTest(entityId);
        List<Guid> stepIds = Arrays.asList(FixturesTool.STEP_ID, FixturesTool.STEP_ID_2);
        stepIds.forEach(x -> {
            Step s = dao.get(x);
            s.setProgress(null);
            s.setStatus(JobExecutionStatus.FINISHED);
            dao.update(s);
        });

        BaseDisk diskImage = diskDao.get(entityId);
        assertProgress(80, diskImage);
    }


    private StepSubjectEntity prepareStartedStepsByStepSubjectEntityTest() {
        Guid entityId = Guid.newGuid();
        VdcObjectType entityType = VdcObjectType.EXECUTION_HOST;
        StepSubjectEntity subjectEntity = new StepSubjectEntity(FixturesTool.STEP_ID, entityType, entityId);
        subjectEntityDao.saveAll(Arrays.asList(subjectEntity));

        Step s = dao.get(FixturesTool.STEP_ID);
        s.setStatus(JobExecutionStatus.STARTED);
        dao.update(s);

        return subjectEntity;
    }

    @Test
    public void startedStepsByStepSubjectEntityNoStepsHaveStep() {
        StepSubjectEntity subjectEntity = prepareStartedStepsByStepSubjectEntityTest();
        List<Step> steps = dao.getStartedStepsByStepSubjectEntity(subjectEntity);
        assertEquals(1, steps.size());
        assertEquals(subjectEntity.getStepId(), steps.get(0).getId());
    }


    @Test
    public void startedStepsByStepSubjectEntityOtherStatuses() {
        StepSubjectEntity subjectEntity = prepareStartedStepsByStepSubjectEntityTest();
        Step step = dao.get(FixturesTool.STEP_ID);
        Arrays.stream(JobExecutionStatus.values())
                .filter(status -> status != JobExecutionStatus.STARTED)
                .forEach(status -> {
                    step.setStatus(status);
                    dao.update(step);
                    assertNoStartedStepsForSubjectEntity(subjectEntity);
                });
    }

    @Test
    public void startedStepsByStepSubjectEntityOtherId() {
        StepSubjectEntity subjectEntity = prepareStartedStepsByStepSubjectEntityTest();
        subjectEntity.setEntityId(Guid.Empty);
        assertNoStartedStepsForSubjectEntity(subjectEntity);
    }

    @Test
    public void startedStepsByStepSubjectEntityOtherEntityTYpe() {
        StepSubjectEntity subjectEntity = prepareStartedStepsByStepSubjectEntityTest();
        subjectEntity.setEntityType(VdcObjectType.AdElements);
        assertNoStartedStepsForSubjectEntity(subjectEntity);
    }

    private void updateStepProgress(Guid stepId, Integer progress) {
        dao.updateStepProgress(stepId, progress);
    }

    private void assertProgress(Integer expectedProgress, BaseDisk disk) {
        assertEquals("disk progress isn't as expected", expectedProgress, disk.getProgress());
    }

    private void assertNoStartedStepsForSubjectEntity(SubjectEntity subjectEntity) {
        assertThat(dao.getStartedStepsByStepSubjectEntity(subjectEntity), emptyCollectionOf(Step.class));
    }
}
