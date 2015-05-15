package com.cisco.oss.foundation.directory.entity.compatible;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.utils.JsonSerializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The unit test for the compatibility of ModelServiceInstance 1.2 vs. 1.1
 */
public class ModelServiceInstanceCompatibleTest {
    private static final Date date = new Date();
    private static ModelServiceInstance testInstance;
    private static String testInstanceStr;

    @BeforeClass
    public static void setUp() throws Exception{
        Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        testInstance = new ModelServiceInstance("odrm", "192.168.1.1", "192.168.1.1", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, "192.168.1.1", 12345, date,
                date, metadata);
        testInstance.setHeartbeatTime(date);
        testInstanceStr = new String(JsonSerializer.serialize(testInstance));
    }

    @Test
    public void testSerialize() throws Exception {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        ModelServiceInstance11 instance11 = new ModelServiceInstance11("odrm", "192.168.1.1", "192.168.1.1", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, "192.168.1.1", 12345, date,
                date, metadata);
        instance11.setHeartbeatTime(date);
        assertEquals(testInstanceStr,new String(JsonSerializer.serialize(instance11)));
    }

    @Test
    public void testDeserialize() throws Exception {

        String hasPort = testInstanceStr;
        assertTrue(hasPort.indexOf(",\"port\":12345")>0);

        ModelServiceInstance11 instance11HasPort = JsonSerializer.deserialize(hasPort.getBytes(), ModelServiceInstance11.class);

        assertEquals(12345, instance11HasPort.getPort());

        String withoutPort = hasPort.replace(",\"port\":12345", "");


        ModelServiceInstance11 instance11NoPort = JsonSerializer.deserialize(withoutPort.getBytes(), ModelServiceInstance11.class);

        assertEquals(0, instance11NoPort.getPort()); //if no port on JSON string. the 1.1 instance will get a default zero

        ModelServiceInstance12 instance12 = JsonSerializer.deserialize(withoutPort.getBytes(), ModelServiceInstance12.class);

        assertEquals(withoutPort,new String(JsonSerializer.serialize(instance12)));


    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ModelServiceInstance12 {

        /**
         * The id.
         */
        private String id;

        /**
         * The instance id.
         */
        private String instanceId;

        /**
         * The instance uri.
         */
        private String uri;

        /**
         * The instance OperationalStatus
         */
        private OperationalStatus status;

        /**
         * Whether the instance is monitored or not in Service Directory.
         */
        private boolean monitorEnabled = true;

        /**
         * The instance creation time.
         */
        private Date createTime;

        /**
         * The instance last modified time.
         */
        private Date modifiedTime;

        /**
         * The instance last heartbeat time.
         */
        private Date heartbeatTime;

        /**
         * The instance metadata info.
         */
        private Map<String, String> metadata;

        /**
         * The instance service name.
         */
        private String serviceName;

        /**
         * The real address of the instance, it can be real IP or host name
         */
        private String address;

        /**
         * Constructor.
         */
        public ModelServiceInstance12() {

        }

        /**
         * Constructor.
         *
         * @param serviceName  the service name.
         * @param instanceId   the instance id.
         * @param id           the id.
         * @param uri          the uri of the ServiceInstance.
         * @param status       the OperationalStatus.
         * @param address      The real address of the instance, it can be real IP or host
         *                     name.
         * @param modifiedTime the last modified time stamp.
         * @param createTime   the create time stamp.
         * @param metadata     the metadata map.
         */
        public ModelServiceInstance12(String serviceName, String instanceId,
                                    String id, String uri, OperationalStatus status, String address,
                                    Date modifiedTime, Date createTime,
                                    Map<String, String> metadata) {
            this.serviceName = serviceName;
            this.instanceId = instanceId;
            this.id = id;
            this.uri = uri;
            this.status = status;
            this.metadata = metadata;
            this.modifiedTime = modifiedTime;
            this.createTime = createTime;
            this.address = address;
        }

        /**
         * Get the id.
         *
         * @return the id.
         */
        public String getId() {
            return id;
        }

        /**
         * Set the id.
         *
         * @param id the id.
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Get the instance id.
         *
         * @return the instance id.
         */
        public String getInstanceId() {
            return instanceId;
        }

        /**
         * Set the instance id.
         *
         * @param instanceId the instance id.
         */
        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }

        /**
         * Get the URI.
         *
         * @return the URI.
         */
        public String getUri() {
            return uri;
        }

        /**
         * Set the URI.
         *
         * @param uri the URI.
         */
        public void setUri(String uri) {
            this.uri = uri;
        }

        /**
         * Get the real address, it can be real IP or host name.
         *
         * @return the real address.
         */
        public String getAddress() {
            return address;
        }

        /**
         * Set the real address, it can be real IP or host name.
         *
         * @param address the real address.
         */
        public void setAddress(String address) {
            this.address = address;
        }

        /**
         * Get the OperationalStatus.
         *
         * @return the OperationalStatus.
         */
        public OperationalStatus getStatus() {
            return status;
        }

        /**
         * Set the OperationalStatus.
         *
         * @param status the OperationalStatus.
         */
        public void setStatus(OperationalStatus status) {
            this.status = status;
        }

        /**
         * check is monitor enable in Service Directory.
         *
         * @return true if monitor enabled.
         */
        public boolean isMonitorEnabled() {
            return monitorEnabled;
        }

        /**
         * Set the monitor.
         *
         * @param monitor the monitor.
         */
        public void setMonitorEnabled(boolean monitor) {
            this.monitorEnabled = monitor;
        }

        /**
         * Get the create time stamp.
         *
         * @return the create time stamp.
         */
        public Date getCreateTime() {
            return createTime;
        }

        /**
         * Set the create time stamp.
         *
         * @param createTime the create time stamp.
         */
        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        /**
         * Get the last modified time stamp.
         *
         * @return the last modified time stamp.
         */
        public Date getModifiedTime() {
            return modifiedTime;
        }

        /**
         * Set the last modified time stamp.
         *
         * @param modifiedTime the last modified time stamp.
         */
        public void setModifiedTime(Date modifiedTime) {
            this.modifiedTime = modifiedTime;
        }

        /**
         * Get the last heartbeat time stamp.
         *
         * @return the last heartbeat time stamp.
         */
        public Date getHeartbeatTime() {
            return heartbeatTime;
        }

        /**
         * Set the last heartbeat time stamp.
         *
         * @param heartbeatTime the last heartbeat time stamp.
         */
        public void setHeartbeatTime(Date heartbeatTime) {
            this.heartbeatTime = heartbeatTime;
        }

        /**
         * Get the Metadata Map.
         *
         * @return the Metadata Map.
         */
        public Map<String, String> getMetadata() {
            return metadata;
        }

        /**
         * Set the Metadata Map.
         *
         * @param metadata the Metadata Map.
         */
        public void setMetadata(Map<String, String> metadata) {
            this.metadata = metadata;
        }

        /**
         * Get the service name.
         *
         * @return the service name.
         */
        public String getServiceName() {
            return serviceName;
        }

        /**
         * Set the service name.
         *
         * @param serviceName the service name.
         */
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModelServiceInstance12 that = (ModelServiceInstance12) o;
            return Objects.equals(monitorEnabled, that.monitorEnabled) &&
                    Objects.equals(id, that.id) &&
                    Objects.equals(instanceId, that.instanceId) &&
                    Objects.equals(uri, that.uri) &&
                    Objects.equals(status, that.status) &&
                    Objects.equals(createTime, that.createTime) &&
                    Objects.equals(modifiedTime, that.modifiedTime) &&
                    Objects.equals(heartbeatTime, that.heartbeatTime) &&
                    Objects.equals(metadata, that.metadata) &&
                    Objects.equals(serviceName, that.serviceName) &&
                    Objects.equals(address, that.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, instanceId, uri, status, monitorEnabled, createTime, modifiedTime, heartbeatTime, metadata, serviceName, address);
        }

        @Override
        public String toString() {
            return "ModelServiceInstance{" +
                    "id='" + id + '\'' +
                    ", instanceId='" + instanceId + '\'' +
                    ", uri='" + uri + '\'' +
                    ", status=" + status +
                    ", monitorEnabled=" + monitorEnabled +
                    ", createTime=" + (createTime == null ? "null" : createTime.getTime()) +
                    ", modifiedTime=" + (modifiedTime == null ? "null" : modifiedTime.getTime()) +
                    ", heartbeatTime=" + (heartbeatTime == null ? "null" : heartbeatTime.getTime()) +
                    ", metadata=" + metadata +
                    ", serviceName='" + serviceName + '\'' +
                    ", address='" + address + '\'' +
                    '}';
        }
    }

}
