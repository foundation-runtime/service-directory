package com.cisco.oss.foundation.directory.client;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for DirectoryServiceInMemoryClient
 */
public class DirectoryServiceInMemoryClientTest {

    private final static Logger LOG = LoggerFactory.getLogger(DirectoryServiceInMemoryClientTest.class);
    private final DirectoryServiceInMemoryClient sharedMemoryClient = new DirectoryServiceInMemoryClient();

    @Test
    public void testRegisterInstanceExist() throws InterruptedException, ExecutionException {
        final long start = System.currentTimeMillis();
        final ProvidedServiceInstance instance = new ProvidedServiceInstance("fooService", "192.168.1.1", 8080);
        instance.setMonitorEnabled(true);
        instance.setStatus(OperationalStatus.UP);

        final ProvidedServiceInstance instance2 = new ProvidedServiceInstance("fooService", "192.168.1.2", 8080);
        final ProvidedServiceInstance instance3 = new ProvidedServiceInstance("fooService2", "192.168.1.3", 8080);
        final ProvidedServiceInstance instance4 = new ProvidedServiceInstance("fooService2", "192.168.1.4", 8080);
        final ProvidedServiceInstance instance5 = new ProvidedServiceInstance("fooService2", "192.168.1.5", 8080);
        ExecutorService exec = Executors.newFixedThreadPool(10);
        CountDownLatch regCountDown = new CountDownLatch(5 * 5);
        CountDownLatch fdAllCountDown = new CountDownLatch(5*3);
        CountDownLatch updateCountDown = new CountDownLatch(5 * 5);
        CountDownLatch unregCountDown = new CountDownLatch(5 * 3);
        for (int i = 0; i < 5; i++) {
            exec.submit(new Register(instance, regCountDown));
            exec.submit(new Register(instance2, regCountDown));
            exec.submit(new Register(instance3, regCountDown));
            exec.submit(new Register(instance4, regCountDown));
            exec.submit(new Register(instance5, regCountDown));
        }
        regCountDown.await();
        for (ModelServiceInstance ins : sharedMemoryClient.getAllInstances()){
            System.out.println(ins);
            //Never depends on hashcode() to identify a object.
            // a == b => a.hashcode() == b.hashcode();
            // BUT !
            // a.hashcode()==b.hashcode() !=> a == b
            // System.out.println(Integer.toHexString(System.identityHashCode(ins)));
            System.out.println(ins.getInstanceId());
        }

        System.out.println("");
        for (int i = 0; i<5; i++){
            exec.submit(new FindAll(fdAllCountDown));
            exec.submit(new Update(updateCountDown).update("192.168.1.1", 8080));
            exec.submit(new Update(updateCountDown).update("192.168.1.2", 8080));
            exec.submit(new Update(updateCountDown).update("192.168.1.3", 8080));
            exec.submit(new Update(updateCountDown).update("192.168.1.4", 8080));
            exec.submit(new Update(updateCountDown).update("192.168.1.5", 8080));
            exec.submit(new FindAll(fdAllCountDown));
            exec.submit(new Unregister(instance3, unregCountDown));
            exec.submit(new Unregister(instance4, unregCountDown));
            exec.submit(new Unregister(instance5, unregCountDown));
            exec.submit(new FindAll(fdAllCountDown));
        }
        fdAllCountDown.await();
        updateCountDown.await();
        unregCountDown.await();

        final long end = System.currentTimeMillis();
        // only one
        assertEquals(2, sharedMemoryClient.getAllInstances().size());

        System.out.println("Final Registry Size : " + sharedMemoryClient.getAllInstances().size());
        System.out.println("During(ms) : " + (end - start));
    }

    abstract class CountDownCall<T> implements Callable<T> {

        private final CountDownLatch countDown;

        CountDownCall(CountDownLatch countDown) {
            this.countDown = countDown;
        }

        protected void countDown() {
            countDown.countDown();
        }

        abstract T doCalling();

        @Override
        public T call() throws Exception {
            T result = doCalling();
            countDown();
            return result;
        }
    }

    class Register extends CountDownCall {
        private final ProvidedServiceInstance instance;

        Register(ProvidedServiceInstance instance, CountDownLatch countDown) {
            super(countDown);
            this.instance = instance;
        }

        @Override
        public Boolean doCalling() {
            sharedMemoryClient.registerInstance(instance);
            return true;
        }
    }

    class FindAll extends CountDownCall {
        FindAll(CountDownLatch countDown) {
            super(countDown);
        }

        @Override
        public Integer doCalling() {
            Integer size = sharedMemoryClient.getAllInstances().size();
            LOG.debug("In thread " + Thread.currentThread().getName() + " Registry size=" + size);
            return size;
        }
    }

    class Update extends CountDownCall {
        public ProvidedServiceInstance instanceToUpdate = new ProvidedServiceInstance("fooService", null, -1);

        Update(CountDownLatch countDown) {
            super(countDown);
        }

        public Update update(String address, Integer port) {
            instanceToUpdate.setAddress(address);
            instanceToUpdate.setPort(port);
            return this;
        }

        @Override
        public Boolean doCalling() {
            sharedMemoryClient.updateInstance(instanceToUpdate);
            return true;
        }
    }

    class Unregister extends CountDownCall {
        private final ProvidedServiceInstance instance;

        Unregister(ProvidedServiceInstance instance, CountDownLatch countDown) {
            super(countDown);
            this.instance = instance;
        }

        @Override
        public Boolean doCalling() {
            sharedMemoryClient.unregisterInstance(instance.getServiceName(), instance.getProviderId(), true);
            return true;
        }
    }

}
