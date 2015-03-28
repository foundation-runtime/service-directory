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

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
