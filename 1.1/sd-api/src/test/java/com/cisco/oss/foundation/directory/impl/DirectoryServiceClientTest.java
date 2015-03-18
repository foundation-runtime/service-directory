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
package com.cisco.oss.foundation.directory.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
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
import static com.cisco.oss.foundation.directory.utils.JsonSerializer.*;

public class DirectoryServiceClientTest {

    @BeforeClass
    public static void setup(){

    }

    @Test
    public void testRegisterInstance() throws ServiceException{
        DirectoryServiceClient client = ((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();

        final ProvidedServiceInstance instance = new ProvidedServiceInstance("odrm", "192.168.7.4", 8901);
        instance.setMonitorEnabled(true);
        instance.setStatus(OperationalStatus.UP);
        instance.setUri("http://cisco.com/vbo/odrm/setupsession");
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        instance.setMetadata(metadata);

        HttpUtils utils = new HttpUtils(){
            @Override
            public HttpResponse postJson(String urlStr, String body)
                    throws IOException {
                Assert.assertEquals("http://vcsdirsvc:2013/service/odrm/192.168.7.4-8901", urlStr);
                ProvidedServiceInstance instance2 = (ProvidedServiceInstance)deserialize(body.getBytes(), ProvidedServiceInstance.class);
                compareProvidedServiceInstance(instance, instance2);
                return new HttpResponse(201, null);

            }
        };
        client.getDirectoryInvoker().setHttpUtils(utils);

        client.registerInstance(instance);
    }

    @Test
    public void testupdateInstance() throws ServiceException{
        DirectoryServiceClient client = ((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();

        final ProvidedServiceInstance instance = new ProvidedServiceInstance("odrm", "192.168.7.4", 8901);
        instance.setMonitorEnabled(true);
        instance.setStatus(OperationalStatus.UP);
        instance.setUri("http://cisco.com/vbo/odrm/setupsession");
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        instance.setMetadata(metadata);

        HttpUtils utils = new HttpUtils(){
            @Override
            public HttpResponse putJson(String urlStr, String body)
                    throws IOException {
                Assert.assertEquals("http://vcsdirsvc:2013/service/odrm/192.168.7.4-8901", urlStr);
                ProvidedServiceInstance instance2 = deserialize(body.getBytes(), ProvidedServiceInstance.class);
                compareProvidedServiceInstance(instance, instance2);
                return new HttpResponse(201, null);

            }
        };
        client.getDirectoryInvoker().setHttpUtils(utils);

        client.updateInstance(instance);
    }

    @Test
    public void testupdateInstanceStatus() throws ServiceException{
        DirectoryServiceClient client = ((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();


        final String serviceName = "odrm";
        final String instanceId = "192.168.7.4-8901";
        final OperationalStatus status = OperationalStatus.DOWN;

        HttpUtils utils = new HttpUtils(){
            @Override
            public HttpResponse put(String urlStr, String body, Map<String, String> headers)
                    throws IOException {

                Assert.assertEquals("http://vcsdirsvc:2013/service/" + serviceName + "/" + instanceId + "/status", urlStr);
                Assert.assertEquals(body, "status=" + status + "&isOwned=true");
                return new HttpResponse(200, null);

            }
        };
        client.getDirectoryInvoker().setHttpUtils(utils);

        client.updateInstanceStatus(serviceName, instanceId, status, true);
    }


    @Test
    public void testupdateInstanceUri() throws ServiceException{
        DirectoryServiceClient client = ((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();


        final String serviceName = "odrm";
        final String instanceId = "192.168.7.4-8901";
        final String uri = "http://cisco.com/vbo/odrm/setupsession";

        HttpUtils utils = new HttpUtils(){
            @Override
            public HttpResponse put(String urlStr, String body, Map<String, String> headers)
                    throws IOException {

                Assert.assertEquals("http://vcsdirsvc:2013/service/" + serviceName + "/" + instanceId + "/uri", urlStr);
                Assert.assertEquals(body, "uri=" + URLEncoder.encode(uri, "UTF-8") + "&isOwned=false");
                return new HttpResponse(200, null);

            }
        };
        client.getDirectoryInvoker().setHttpUtils(utils);

        client.updateInstanceUri(serviceName, instanceId, uri, false);
    }

    @Test
    public void testunregisterInstance() throws ServiceException{
        DirectoryServiceClient client = ((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();


        final String serviceName = "odrm";
        final String instanceId = "192.168.7.4-8901";

        HttpUtils utils = new HttpUtils(){
            @Override
            public HttpResponse deleteJson(String urlStr)
                    throws IOException {

                Assert.assertEquals("http://vcsdirsvc:2013/service/" + serviceName + "/" + instanceId + "/true" , urlStr);
                return new HttpResponse(200, null);

            }
        };
        client.getDirectoryInvoker().setHttpUtils(utils);

        client.unregisterInstance(serviceName, instanceId, true);
    }

    @Test
    public void testsendHeartBeat() throws ServiceException{
        DirectoryServiceClient client = ((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();

        final Map<String, ServiceInstanceHeartbeat> heartbeatMap = new HashMap<String, ServiceInstanceHeartbeat>();

        heartbeatMap.put("odrm/192.168.2.3-8901", new ServiceInstanceHeartbeat("odrm", "192.168.2.3-8901"));

        final Map<String, OperationResult<String>> result = new HashMap<String, OperationResult<String>>();
        result.put("odrm/192.168.2.3-8901", new OperationResult<String>(true, "it is OK", null));

        HttpUtils utils = new HttpUtils(){
            @Override
            public HttpResponse putJson(String urlStr, String body)
                    throws IOException {

                Assert.assertEquals("http://vcsdirsvc:2013/service/heartbeat", urlStr);
                Map<String, ServiceInstanceHeartbeat> hbs = deserialize(body.getBytes(), new TypeReference<Map<String, ServiceInstanceHeartbeat>>(){});
                Assert.assertEquals(hbs.size(), 1);
                Assert.assertEquals(hbs.get("odrm/192.168.2.3-8901").getServiceName(), "odrm");
                Assert.assertEquals(hbs.get("odrm/192.168.2.3-8901").getProviderId(), "192.168.2.3-8901");

                return new HttpResponse(200, new String(serialize(result)));

            }
        };
        client.getDirectoryInvoker().setHttpUtils(utils);

        client.sendHeartBeat(heartbeatMap);
    }


    @Test
    public void testgetMetadataKey() throws ServiceException{
        DirectoryServiceClient client = ((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();

        final String keyName = "datacenter";

        Date date = new Date();

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
        ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3-8901", "192.168.2.3-8901", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, null, 0, date,
                date, metadata);
        instance.setHeartbeatTime(date);
        instances.add(instance);
        final ModelMetadataKey result = new ModelMetadataKey("datacenter");
        result.setCreateTime(date);
        result.setModifiedTime(date);
        result.setId("datacenter");
        result.setServiceInstances(instances);

        HttpUtils utils = new HttpUtils(){
            @Override
            public HttpResponse getJson(String urlStr)
                    throws IOException {

                Assert.assertEquals("http://vcsdirsvc:2013/metadatakey/" + keyName, urlStr);

                return new HttpResponse(200, new String(serialize(result)));

            }
        };
        client.getDirectoryInvoker().setHttpUtils(utils);

        ModelMetadataKey metadatakey = client.getMetadataKey(keyName);

        Assert.assertEquals(metadatakey.getName(), "datacenter");
        Assert.assertEquals(metadatakey.getId(), "datacenter");
        Assert.assertEquals(metadatakey.getCreateTime().getTime(), date.getTime());
        Assert.assertEquals(metadatakey.getModifiedTime().getTime(), date.getTime());
        Assert.assertEquals(metadatakey.getServiceInstances().size(), 1);

        compareModelServiceInstance(instance, metadatakey.getServiceInstances().get(0));
    }

    @Test
    public void testlookupService() throws ServiceException{
        DirectoryServiceClient client = ((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();

        final String serviceName = "odrm";

        Date date = new Date();

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
        ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3-8901", "192.168.2.3-8901", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, null, 0, date,
                date, metadata);
        instance.setHeartbeatTime(date);
        instances.add(instance);
        final ModelService result = new ModelService("odrm", "odrm", date);
        result.setServiceInstances(instances);

        HttpUtils utils = new HttpUtils(){
            @Override
            public HttpResponse getJson(String urlStr)
                    throws IOException {

                Assert.assertEquals("http://vcsdirsvc:2013/service/" + serviceName, urlStr);

                return new HttpResponse(200, new String(serialize(result)));

            }
        };
        client.getDirectoryInvoker().setHttpUtils(utils);

        ModelService service = client.lookupService(serviceName);

        Assert.assertEquals(service.getName(), "odrm");
        Assert.assertEquals(service.getId(), "odrm");
        Assert.assertEquals(service.getCreateTime().getTime(), date.getTime());
        Assert.assertEquals(service.getServiceInstances().size(), 1);

        compareModelServiceInstance(instance, service.getServiceInstances().get(0));
    }

    @Test
    public void testgetServiceChanging() throws ServiceException{
        DirectoryServiceClient client = ((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();

        final Date date = new Date();

        Map<String, ModelService> services = new HashMap<String, ModelService>();
        services.put("odrm", new ModelService("odrm", "odrm", date));

        final String serviceName = "odrm";



        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
        ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3-8901", "192.168.2.3-8901", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, null, 0, date,
                date, metadata);
        instance.setHeartbeatTime(date);
        instances.add(instance);
        ModelService changingservice = new ModelService("odrm", "odrm", date);
        changingservice.setServiceInstances(instances);

        final Map<String, OperationResult<ModelService>> result = new HashMap<String, OperationResult<ModelService>>();
        result.put("odrm", new OperationResult<ModelService>(true, changingservice, new ServiceDirectoryError(ErrorCode.OK)));

        HttpUtils utils = new HttpUtils(){
            @Override
            public HttpResponse postJson(String urlStr, String body)
                    throws IOException {

                Assert.assertEquals("http://vcsdirsvc:2013/service/changing", urlStr);

                Map<String, ModelService> target = deserialize(body.getBytes(), new TypeReference<Map<String, ModelService>>(){});
                Assert.assertEquals(target.size(), 1);
                Assert.assertEquals(target.get("odrm").getCreateTime().getTime(), date.getTime());
                Assert.assertEquals(target.get("odrm").getName(), "odrm");

                return new HttpResponse(200, new String(serialize(result)));

            }
        };
        client.getDirectoryInvoker().setHttpUtils(utils);

        Map<String, OperationResult<ModelService>> changed = client.getServiceChanging(services);

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
        Assert.assertEquals(instance1.getProviderId(), instance2.getProviderId());
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
