package com.cisco.oss.foundation.directory.entity;

import java.util.Comparator;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils.toServiceInstance;

/**
 * The class keeps track of the service instance changes in Service Directory.
 *
 * @since 1.2
 */
public class InstanceChange<T> {
    public enum ChangeType {
        Create,
        Remove,
        Status,
        URL,
        META
    }

    public final long changedTimeMills;
    public final ChangeType changeType;
    public final String serviceName;
    public final T from;
    public final T to;

    /**
     * Constructor.
     *
     * @param time
     *            the changed time
     * @param serviceName
     *            the service name
     * @param type
     *            the change type
     * @param from
     *            the old value
     * @param to
     *            the new value            
     *
     */
    @JsonCreator
    public InstanceChange(@JsonProperty("changedTimeMills")long time,
                          @JsonProperty("serviceName")String serviceName,
                          @JsonProperty("changeType")ChangeType type,
                          @JsonProperty("from")T from,
                          @JsonProperty("to")T to) {
        Objects.requireNonNull(time);
        Objects.requireNonNull(serviceName);
        this.changedTimeMills = time;
        this.changeType = type;
        this.serviceName = serviceName;
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        return "ServiceInstanceChange{" +
                "changedTimeMills=" + changedTimeMills +
                ", serviceName=" + serviceName +
                ", changeType=" + changeType +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }

    /**
     * Order by changed time, oldest first
     */
    public static final java.util.Comparator<InstanceChange> Comparator = new Comparator<InstanceChange>() {
        @Override
        public int compare(InstanceChange o1, InstanceChange o2) {
            return Long.compare(o1.changedTimeMills, o2.changedTimeMills);
        }
    };

    /**
     * Order by changed time, latest first
     */
    public static final Comparator<InstanceChange> ReverseComparator = new Comparator<InstanceChange>() {
        @Override
        public int compare(InstanceChange o1, InstanceChange o2) {
            return Comparator.compare(o2, o1);
        }
    };
    
    /**
     * Convert the model service instance change to the service instance change object
     * @param modelInstanceChange
     * @return InstanceChange<ServiceInstance>
     */
    public static InstanceChange<ServiceInstance> toServiceInstanceChange(InstanceChange<ModelServiceInstance> modelInstanceChange){
        Objects.requireNonNull(modelInstanceChange);
        return new InstanceChange<ServiceInstance>(modelInstanceChange.changedTimeMills,
                modelInstanceChange.serviceName,
                modelInstanceChange.changeType,
                modelInstanceChange.from==null?null:toServiceInstance(modelInstanceChange.from),
                modelInstanceChange.to==null?null:toServiceInstance(modelInstanceChange.to));
    }
}
