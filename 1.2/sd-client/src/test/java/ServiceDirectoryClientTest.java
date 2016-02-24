import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServiceDirectoryClientTest {

    Lock sequential = new ReentrantLock();

    @Before
    public void setUp() throws Exception {
        sequential.lock();
    }

    @After
    public void tearDown() throws Exception {
        TimeUnit.SECONDS.sleep(1L);
        sequential.unlock();
    }

    @Test
    public void testNoArgument(){
        ServiceDirectoryClient.main(new String[]{});
    }

    @Test
    public void testHelp(){
        ServiceDirectoryClient.main(new String[]{"-help"});
    }

    @Test
    public void testWrongArguments() throws IOException {
        ServiceDirectoryClient.main(new String[]{"-exe","wrong"});
        ServiceDirectoryClient.main(new String[]{"-help","wrong","-exec","testConnection","test"});
    }

    @Test
    public void testLookup1(){
        ServiceDirectoryClient.main(new String[]{"-exec","getInstanceOf test"});
    }

    @Test
    public void testLookup2(){
        ServiceDirectoryClient.main(new String[]{"-exec","getInstanceOf", "test","test2","-server","192.168.0.1","8080"});
    }

    @Test
    public void testLookup3(){
        ServiceDirectoryClient.main(new String[]{"-exec","lookup", "test","test2","test3","-server","192.168.0.1","8080"});
    }

    @Test
    public void testSpacesInCommandArgs1(){
        ServiceDirectoryClient.main(new String[]{"-exec","getAllServices   "});
    }

    @Test
    public void testSpacesInCommandArgs2(){
        ServiceDirectoryClient.main(new String[]{"-exec","getInstanceOf    test "});
    }



}
