/**
 * Copyright 2015 Cisco Systems, Inc.
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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.exception.ServiceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static com.cisco.oss.foundation.directory.ServiceDirectory.ServiceDirectoryConfig.ClientType.DUMMY;

/**
 * Test for JDK 7 try-with-resource, so that we close resource
 * automatically when we got error
 */
public class AutoCloseTest {

    private final AtomicInteger invokerCount = new AtomicInteger(0);
    private final Exception myCheckedEx = new Exception("myCheckedEx");
    private final RuntimeException myRuntimeEx = new RuntimeException("myRuntimeEx");

    class MyService implements AutoCloseable{

        public void doDirtyJobThrowRuntimeEx(){
           throw myRuntimeEx;
        }
        public void doDirtyJobThrowEx() throws Exception{
           throw myCheckedEx;

        }
        @Override
        public void close(){
            invokerCount.getAndIncrement();
            //System.out.println("closed!");
        }
    }

    @Test
    public void testAutoClosableWhenException() {
        /**
         * JDK 7 try-with-resource
         */
        try(MyService myService = new MyService(); MyService myService2 = new MyService()){
            myService.doDirtyJobThrowRuntimeEx();
            myService2.doDirtyJobThrowEx();
        }catch(Exception e){
            //When ex catch, resource already closed
            assertEquals(2,invokerCount.get()); //although one failure. all two are closed
            assertEquals(myRuntimeEx,e); // runtime ex thrown
        }

        try(MyService myService = new MyService()){
            myService.doDirtyJobThrowEx();
        }catch(Exception e){
            assertEquals(3,invokerCount.get());
            assertEquals(myCheckedEx,e); // checked ex thrown
        }

        /**
         * JDK 6 traditional way
         */
        MyService myService = new MyService();
        try {
            myService.doDirtyJobThrowEx();
        } catch (Exception e) {
            assertEquals(3,invokerCount.get()); //when exception throw, resource not closed
            assertEquals(myCheckedEx,e);
        } finally {
            myService.close();
        }
        assertEquals(4,invokerCount.get()); //closed after finally called
    }

    @Test
    public void testAutoCloseForLookupMangerNoCache(){
        LookupManager lookupMgr = ServiceDirectory.config().setCacheEnabled(false).build().getLookupManager();
        assertTrue(lookupMgr.isStarted()); //started
        lookupMgr.close(); //explicitly close fail
        assertFalse(lookupMgr.isStarted());


        try (LookupManager lookupManager = ServiceDirectory.config().setCacheEnabled(false).build().getLookupManager()) {
            assertTrue(lookupManager.isStarted()); //started
        }//auto-closed

    }

    @Test
    public void testAutoCloseForCachedLookupManagerSingle() throws Exception {

        LookupManager lookupMgr = ServiceDirectory.config().setClientType(DUMMY).build().getLookupManager();
        TimeUnit.SECONDS.sleep(3L);
        lookupMgr.close(); //explicitly close

        try (LookupManager lookupManager2 = ServiceDirectory.config().setClientType(DUMMY).build().getLookupManager()) {
            TimeUnit.SECONDS.sleep(3L);
            assertTrue(lookupManager2.isStarted()); //started
        }
        //Auto-close OK

        //Use the same SD instance
    }
    @Test
    public void testAutoCloseForCachedLookupManagerMultiple() throws Exception{

        ConfigurableServiceDirectoryManagerFactory instance = ServiceDirectory.config().setClientType(DUMMY).build();
        try(LookupManager mgr1 = instance.getLookupManager(); LookupManager mgr2 = instance.getLookupManager()){
            TimeUnit.SECONDS.sleep(3L);
            assertTrue(mgr1.isStarted());
            assertTrue(mgr2.isStarted());
        }
        // cache service is stopped only when all mgrs closed

        ConfigurableServiceDirectoryManagerFactory instance2 = ServiceDirectory.config().setClientType(DUMMY).build();
        TimeUnit.SECONDS.sleep(3L);
        assertTrue(instance2.getLookupManager().isStarted());
        assertTrue(instance2.getLookupManager().isStarted());
        // cache service shutdown is not called


        ConfigurableServiceDirectoryManagerFactory instance3 = ServiceDirectory.config().setClientType(DUMMY).build();
        TimeUnit.SECONDS.sleep(3L);
        LookupManager m1 =  instance3.getLookupManager();
        LookupManager m2 =  instance3.getLookupManager();
        assertTrue(m1.isStarted());
        assertTrue(m2.isStarted());
        m1.close();
        m2.close();
        // cache service is shutdown if all mgr are explicitly closed()

    }

    @Test
    public void testAutoCloseForRegistrationManager(){
        RegistrationManager rMgr = ServiceDirectory.config().build().getRegistrationManager();
        rMgr.close();

        try (RegistrationManager regMgr = ServiceDirectory.config().build().getRegistrationManager()){
            regMgr.updateServiceUri("foo","foo","foo"); //error
            fail(); //can't go here
        }catch(ServiceException e){};
        //resource auto-released

        try (RegistrationManager noHeartBeatRegMgr = ServiceDirectory.config().setHeartbeatEnabled(false)
                .build().getRegistrationManager()){
            noHeartBeatRegMgr.updateServiceUri("foo", "foo", "foo"); //error
            fail();
        }catch(ServiceException e){};
    }
}
