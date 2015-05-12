/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.directory.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient.DirectoryHttpInvoker;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceHeartbeat;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.utils.HttpResponse;
import com.cisco.oss.foundation.directory.utils.HttpUtils;

public class DirectoryServiceRestfulClientTest {

    @BeforeClass
    public static void setup(){

    }

    @Test
    public void testRegisterInstance() throws Exception{
        final DirectoryServiceRestfulClient client = new DirectoryServiceRestfulClient();

        final ProvidedServiceInstance instance = new ProvidedServiceInstance("odrm", "192.168.7.4");
        instance.setMonitorEnabled(true);
        instance.setStatus(OperationalStatus.UP);
        instance.setUri("http://cisco.com/vbo/odrm/setupsession");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        instance.setMetadata(metadata);

        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker(){
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                Assert.assertEquals("http://vcsdirsvc:2013/service/odrm/192.168.7.4",directoryAddresses+
                        uri);
                ProvidedServiceInstance instance2 = client.deserialize(payload, ProvidedServiceInstance.class);
                compareProvidedServiceInstance(instance, instance2);
                return new HttpResponse(201, null);
            }
        };
        client.setInvoker(mockInvoker);
        client.registerInstance(instance);
    }

    @Test
    public void testUpdateInstance() throws ServiceException{
        final DirectoryServiceRestfulClient client = new DirectoryServiceRestfulClient();

        final ProvidedServiceInstance instance = new ProvidedServiceInstance("odrm", "192.168.7.4");
        instance.setMonitorEnabled(true);
        instance.setStatus(OperationalStatus.UP);
        instance.setUri("http://cisco.com/vbo/odrm/setupsession");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        instance.setMetadata(metadata);

        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker(){
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                Assert.assertEquals("http://vcsdirsvc:2013/service/odrm/192.168.7.4", directoryAddresses+uri);
                ProvidedServiceInstance instance2 = client.deserialize(payload, ProvidedServiceInstance.class);
                compareProvidedServiceInstance(instance, instance2);
                return new HttpResponse(201, null);
            }
        };
        client.setInvoker(mockInvoker);
        client.updateInstance(instance);
    }

    @Test
    public void testUpdateInstanceStatus() throws ServiceException{
        final DirectoryServiceRestfulClient client = new DirectoryServiceRestfulClient();


        final String serviceName = "odrm";
        final String instanceId = "192.168.7.4";
        final OperationalStatus status = OperationalStatus.DOWN;

        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker() {
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                Assert.assertEquals("http://vcsdirsvc:2013/service/" + serviceName + "/" + instanceId + "/status", directoryAddresses+uri);
                Assert.assertEquals(payload, "status=" + status + "&isOwned=true");
                return new HttpResponse(200, null);
            }
        };
        client.setInvoker(mockInvoker);
        client.updateInstanceStatus(serviceName, instanceId, status, true);
    }


    @Test
    public void testUpdateInstanceUri() throws ServiceException,UnsupportedEncodingException{
        final DirectoryServiceRestfulClient client = new DirectoryServiceRestfulClient();


        final String serviceName = "odrm";
        final String instanceId = "192.168.7.4";
        final String uri = "http://cisco.com/vbo/odrm/setupsession";

        final String encodedUri = URLEncoder.encode(uri, "UTF-8");
        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker() {
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                Assert.assertEquals("http://vcsdirsvc:2013/service/" + serviceName + "/" + instanceId + "/uri", directoryAddresses+uri);
                Assert.assertEquals(payload, "uri=" + encodedUri + "&isOwned=false");
                return new HttpResponse(200, null);
            }
        };
        client.setInvoker(mockInvoker);

        client.updateInstanceUri(serviceName, instanceId, uri, false);
    }

    @Test
    public void testUnregisterInstance() throws ServiceException{
        final DirectoryServiceRestfulClient client = new DirectoryServiceRestfulClient();


        final String serviceName = "odrm";
        final String instanceAddress = "192.168.7.4";

        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker() {
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                Assert.assertEquals("http://vcsdirsvc:2013/service/" + serviceName + "/" + instanceAddress + "/true" , directoryAddresses+uri);
                return new HttpResponse(200, null);
            }
        };
        client.setInvoker(mockInvoker);

        client.unregisterInstance(serviceName, instanceAddress, true);
    }

    @Test
    public void testSendHeartBeat() throws ServiceException{
        final DirectoryServiceRestfulClient client = new DirectoryServiceRestfulClient();

        final Map<String, ServiceInstanceHeartbeat> heartbeatMap = new HashMap<>();

        heartbeatMap.put("odrm/192.168.2.3", new ServiceInstanceHeartbeat("odrm", "192.168.2.3"));

        final Map<String, OperationResult<String>> result = new HashMap<>();
        result.put("odrm/192.168.2.3", new OperationResult<>(true, "it is OK", null));

        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker() {
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                Assert.assertEquals("http://vcsdirsvc:2013/service/heartbeat", directoryAddresses+uri);
                Map<String, ServiceInstanceHeartbeat> hbs = client.deserialize(payload, new TypeReference<Map<String, ServiceInstanceHeartbeat>>() {
                });
                Assert.assertEquals(hbs.size(), 1);
                Assert.assertEquals(hbs.get("odrm/192.168.2.3").getServiceName(), "odrm");
                Assert.assertEquals(hbs.get("odrm/192.168.2.3").getProviderAddress(), "192.168.2.3");
                return new HttpResponse(200, client.serialize(result));
            }
        };

        client.setInvoker(mockInvoker);
        client.sendHeartBeat(heartbeatMap);
    }


    @Test
    public void testGetMetadataKey() throws ServiceException{
        final DirectoryServiceRestfulClient client = new DirectoryServiceRestfulClient();

        final String keyName = "datacenter";

        Date date = new Date();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        List<ModelServiceInstance> instances = new ArrayList<>();
        ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3", "192.168.2.3", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, null, 0, date,
                date, metadata);
        instance.setHeartbeatTime(date);
        instances.add(instance);
        final ModelMetadataKey result = new ModelMetadataKey("datacenter");
        result.setCreateTime(date);
        result.setModifiedTime(date);
        result.setId("datacenter");
        result.setServiceInstances(instances);

        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker() {
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                Assert.assertEquals("http://vcsdirsvc:2013/metadatakey/" + keyName, directoryAddresses+uri);
                return new HttpResponse(200, client.serialize(result));
            }
        };

        client.setInvoker(mockInvoker);

        ModelMetadataKey metadatakey = client.getMetadataKey(keyName);

        Assert.assertEquals(metadatakey.getName(), "datacenter");
        Assert.assertEquals(metadatakey.getId(), "datacenter");
        Assert.assertEquals(metadatakey.getCreateTime().getTime(), date.getTime());
        Assert.assertEquals(metadatakey.getModifiedTime().getTime(), date.getTime());
        Assert.assertEquals(metadatakey.getServiceInstances().size(), 1);

        compareModelServiceInstance(instance, metadatakey.getServiceInstances().get(0));
    }

    @Test
    public void testLookupService() throws ServiceException{
        final DirectoryServiceRestfulClient client = new DirectoryServiceRestfulClient();

        final String serviceName = "odrm";

        Date date = new Date();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        List<ModelServiceInstance> instances = new ArrayList<>();
        ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3", "192.168.2.3", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, null, 0, date,
                date, metadata);
        instance.setHeartbeatTime(date);
        instances.add(instance);
        final ModelService result = new ModelService("odrm", "odrm", date);
        result.setServiceInstances(instances);

        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker() {
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                Assert.assertEquals("http://vcsdirsvc:2013/service/" + serviceName, directoryAddresses+uri);
                return new HttpResponse(200, client.serialize(result));
            }
        };
        client.setInvoker(mockInvoker);

        ModelService service = client.lookupService(serviceName);

        Assert.assertEquals(service.getName(), "odrm");
        Assert.assertEquals(service.getId(), "odrm");
        Assert.assertEquals(service.getCreateTime().getTime(), date.getTime());
        Assert.assertEquals(service.getServiceInstances().size(), 1);

        compareModelServiceInstance(instance, service.getServiceInstances().get(0));
    }

    @Test
    public void testGetServiceChanging() throws ServiceException{
        final DirectoryServiceRestfulClient client = new DirectoryServiceRestfulClient();

        final Date date = new Date();

        Map<String, ModelService> services = new HashMap<>();
        services.put("odrm", new ModelService("odrm", "odrm", date));

        Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        List<ModelServiceInstance> instances = new ArrayList<>();
        ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3", "192.168.2.3", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, null, 0, date,
                date, metadata);
        instance.setHeartbeatTime(date);
        instances.add(instance);
        ModelService changingservice = new ModelService("odrm", "odrm", date);
        changingservice.setServiceInstances(instances);

        final Map<String, OperationResult<ModelService>> result = new HashMap<>();
        result.put("odrm", new OperationResult<>(true, changingservice, new ServiceDirectoryError(ErrorCode.OK)));

        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker() {
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                Assert.assertEquals("http://vcsdirsvc:2013/service/changing", directoryAddresses+uri);

                Map<String, ModelService> target = client.deserialize(payload, new TypeReference<Map<String, ModelService>>() {
                });
                Assert.assertEquals(target.size(), 1);
                Assert.assertEquals(target.get("odrm").getCreateTime().getTime(), date.getTime());
                Assert.assertEquals(target.get("odrm").getName(), "odrm");

                return new HttpResponse(200, client.serialize(result));

            }
        };
        client.setInvoker(mockInvoker);

        Map<String, OperationResult<ModelService>> changed = client.getChangedServices(services);

        Assert.assertEquals(changed.size(), 1);
        Assert.assertNotNull(changed.get("odrm"));
        Assert.assertEquals(changed.get("odrm").getResult(), true);
        Assert.assertEquals(changed.get("odrm").getError().getExceptionCode(), ErrorCode.OK);

        ModelService service = changed.get("odrm").getobject();

        Assert.assertEquals(service.getName(), "odrm");
        Assert.assertEquals(service.getId(), "odrm");
        Assert.assertEquals(service.getCreateTime().getTime(), date.getTime());
        Assert.assertEquals(service.getServiceInstances().size(), 1);

        compareModelServiceInstance(instance, service.getServiceInstances().get(0));
    }



    private void compareProvidedServiceInstance(ProvidedServiceInstance instance1, ProvidedServiceInstance instance2){
        Assert.assertNotNull(instance1);
        Assert.assertNotNull(instance2);
        Assert.assertEquals(instance1.getServiceName(), instance2.getServiceName());
        Assert.assertEquals(instance1.getAddress(), instance2.getAddress());
        Assert.assertEquals(instance1.getUri(), instance2.getUri());
        Assert.assertEquals(instance1.getStatus(), instance2.getStatus());
        Map<String, String> metadata1 = instance1.getMetadata();
        Map<String, String> metadata2 = instance2.getMetadata();

        for(Entry<String, String> entry: metadata1.entrySet()){
            Assert.assertEquals(entry.getValue(), metadata2.get(entry.getKey()));
        }

        Assert.assertEquals(metadata1.size(), metadata2.size());
    }

    private void compareModelServiceInstance(ModelServiceInstance instance1, ModelServiceInstance instance2){
        Assert.assertNotNull(instance1);
        Assert.assertNotNull(instance2);
        Assert.assertEquals(instance1.getServiceName(), instance2.getServiceName());
        Assert.assertEquals(instance1.getInstanceId(), instance2.getInstanceId());
        Assert.assertEquals(instance1.getId(), instance2.getId());
        Assert.assertEquals(instance1.getUri(), instance2.getUri());
        Assert.assertEquals(instance1.getStatus(), instance2.getStatus());
        Assert.assertEquals(instance1.getCreateTime().getTime(), instance2.getCreateTime().getTime());
        Assert.assertEquals(instance1.getModifiedTime().getTime(), instance2.getModifiedTime().getTime());
        Assert.assertEquals(instance1.getHeartbeatTime().getTime(), instance2.getHeartbeatTime().getTime());
        Map<String, String> metadata1 = instance1.getMetadata();
        Map<String, String> metadata2 = instance2.getMetadata();

        for(Entry<String, String> entry: metadata1.entrySet()){
            Assert.assertEquals(entry.getValue(), metadata2.get(entry.getKey()));
        }

        Assert.assertEquals(metadata1.size(), metadata2.size());
    }

}
