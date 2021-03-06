package org.ovirt.engine.core.bll.scheduling.policyunits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;

@ExtendWith(MockitoExtension.class)
public class VmToHostAffinityFilterPolicyUnitTest extends VmToHostAffinityPolicyUnitBaseTest {

    @InjectMocks
    VmToHostAffinityFilterPolicyUnit unit =
            new VmToHostAffinityFilterPolicyUnit(null, null);

    @Test
    public void testNoAffinityGroups() {
        hosts = Arrays.asList(host_positive_enforcing, host_negative_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = new ArrayList<>();
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).contains(
                host_positive_enforcing,
                host_negative_enforcing,
                host_not_in_affinity_group);
    }

    @Test
    public void testPositiveAffinity() {
        hosts = Arrays.asList(host_positive_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = Arrays.asList(positive_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).contains
                (host_positive_enforcing).doesNotContain(host_not_in_affinity_group);
    }

    @Test
    public void testNegativeAffinity() {
        hosts = Arrays.asList(host_negative_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = Arrays.asList(negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).contains
                (host_not_in_affinity_group);
    }

    @Test
    public void testNegativeAndPositiveAffinity() {
        hosts = Arrays.asList(host_positive_enforcing, host_negative_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = Arrays.asList(positive_enforcing_group, negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages()))
                .contains(host_positive_enforcing)
                .doesNotContain(host_negative_enforcing);
    }

    @Test
    public void testWithAffinityIntersection() {

        AffinityGroup positiveCollisionGroup = new AffinityGroup();
        positiveCollisionGroup.setVdsIds(Arrays.asList(host_negative_enforcing.getId()));
        positiveCollisionGroup.setVdsAffinityRule(EntityAffinityRule.POSITIVE);
        positiveCollisionGroup.setVdsEnforcing(true);

        hosts = Arrays.asList(host_positive_enforcing, host_negative_enforcing, host_not_in_affinity_group);

        List<AffinityGroup> affinityGroups = Arrays.asList(positiveCollisionGroup, negative_enforcing_group);
        doReturn(affinityGroups).when(affinityGroupDao).getAllAffinityGroupsByVmId(any());

        assertThat(unit.filter(context, hosts, vm, new PerHostMessages())).isEmpty();
    }
}
