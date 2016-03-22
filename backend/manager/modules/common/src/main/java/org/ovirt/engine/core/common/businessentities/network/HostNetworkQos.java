package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.ConfiguredRange;

/**
 * <a href="http://www.ovirt.org/develop/release-management/features/network/detailed-host-network-qos/">wiki doc</a>
 */
public class HostNetworkQos extends QosBase {

    private static final long serialVersionUID = -5062624700835301848L;

    /**
     * Unit: Mbps
     */
    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.MaxHostNetworkQosShares,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outAverageLinkshare;

    /**
     * Unit: Mbps
     */
    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.MaxAverageNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outAverageUpperlimit;

    /**
     * Unit: Mbps
     */
    @ConfiguredRange(min = 1, maxConfigValue = ConfigValues.MaxAverageNetworkQoSValue,
            message = "ACTION_TYPE_FAILED_NETWORK_QOS_OUT_OF_RANGE_VALUES")
    private Integer outAverageRealtime;

    public HostNetworkQos() {
        super(QosType.HOSTNETWORK);
    }

    public Integer getOutAverageLinkshare() {
        return outAverageLinkshare;
    }

    public void setOutAverageLinkshare(Integer outAverageLinkshare) {
        this.outAverageLinkshare = outAverageLinkshare;
    }

    public Integer getOutAverageUpperlimit() {
        return outAverageUpperlimit;
    }

    public void setOutAverageUpperlimit(Integer outAverageUpperlimit) {
        this.outAverageUpperlimit = outAverageUpperlimit;
    }

    public Integer getOutAverageRealtime() {
        return outAverageRealtime;
    }

    public void setOutAverageRealtime(Integer outAverageRealtime) {
        this.outAverageRealtime = outAverageRealtime;
    }

    public boolean isEmpty() {
        return getOutAverageLinkshare() == null && getOutAverageUpperlimit() == null && getOutAverageRealtime() == null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("outAverageLinkshare", renderQosParameter(outAverageLinkshare))
                .append("outAverageUpperlimit", renderQosParameter(outAverageUpperlimit))
                .append("outAverageRealtime", renderQosParameter(outAverageRealtime))
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                outAverageLinkshare,
                outAverageRealtime,
                outAverageUpperlimit
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HostNetworkQos)) {
            return false;
        }
        HostNetworkQos other = (HostNetworkQos) obj;
        return super.equals(obj)
                && Objects.equals(outAverageLinkshare, other.outAverageLinkshare)
                && Objects.equals(outAverageUpperlimit, other.outAverageUpperlimit)
                && Objects.equals(outAverageRealtime, other.outAverageRealtime);
    }

}
