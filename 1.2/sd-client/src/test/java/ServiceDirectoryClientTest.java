import java.io.IOException;

import org.junit.Test;

/**
 * Created by alex on 2/17/16.
 */
public class ServiceDirectoryClientTest {
    @Test
    public void testNoArgument(){
        ServiceDirectoryClient client = new ServiceDirectoryClient();
        client.main(new String[]{});
    }

    @Test
    public void testHelp(){
        ServiceDirectoryClient client = new ServiceDirectoryClient();
        client.main(new String[]{"-help"});
    }

    @Test
    public void testWrongArguments() throws IOException {
        ServiceDirectoryClient client = new ServiceDirectoryClient();
        client.main(new String[]{"-exe","wrong"});
        client.main(new String[]{"-help","wrong","-exec","testConnection","test"});
    }

    @Test
    public void testConnection(){
        ServiceDirectoryClient client = new ServiceDirectoryClient();
        client.main(new String[]{"-exec","testConnection"});
        client.main(new String[]{"-exec","testConnection","-server","localhost:8080"});
    }

    @Test
    public void testLookup1(){
        ServiceDirectoryClient client = new ServiceDirectoryClient();
        client.main(new String[]{"-exec","getInstanceOf test"});
    }

    @Test
    public void testLookup2(){
        ServiceDirectoryClient client = new ServiceDirectoryClient();
        client.main(new String[]{"-exec","getInstanceOf", "test","test2","-server","192.168.0.1","8080"});
    }

    @Test
    public void testLookup3(){
        ServiceDirectoryClient client = new ServiceDirectoryClient();
        client.main(new String[]{"-exec","lookup", "test","test2","test3","-server","192.168.0.1","8080"});
    }
}
