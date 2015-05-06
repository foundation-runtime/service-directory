package com.cisco.oss.foundation.directory.entity;

import java.util.Comparator;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class record the service instance change in Service Directory..
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
                ", changeType=" + changeType +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }

    /**
     * Order by changedTimeMills. oldest first
     */
    public static final java.util.Comparator<InstanceChange> Comparator = new Comparator<InstanceChange>() {
        @Override
        public int compare(InstanceChange o1, InstanceChange o2) {
            return Long.compare(o1.changedTimeMills, o2.changedTimeMills);
        }
    };

    /**
     * latest first
     */
    public static final Comparator<InstanceChange> ReverseComparator = new Comparator<InstanceChange>() {
        @Override
        public int compare(InstanceChange o1, InstanceChange o2) {
            return Comparator.compare(o2, o1);
        }
    };
}
