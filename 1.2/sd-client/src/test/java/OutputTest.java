import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OutputTest {
    @Test
    public void testOutput() throws IOException {
        String location = "/tmp/test";
        File test = new File(location);
        ServiceDirectoryClient.main(new String[]{"-help","-o", location});
        assertTrue(test.exists());
        assertTrue(test.isFile());
        BufferedReader reader = Files.newBufferedReader(test.toPath(), Charset.defaultCharset());
        String line; int num=0;
        System.out.printf("========================= \n");
        System.out.printf("output file : %s \n", location);
        System.out.printf("------------------------- \n");
        while((line=reader.readLine())!=null)
        {
            System.out.printf("%d %s \n",++num,line);
        }
        if (test.exists()){
           Files.delete(test.toPath());
        }
        assertFalse(test.exists());

    }

    @Test
    public void testPermissionDenied() throws IOException {
        String location = "/root/test";
        File test = new File(location);
        ServiceDirectoryClient.main(new String[]{"-help","-o",location});
        assertFalse(test.exists());
    }
}
