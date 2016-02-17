/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ServiceException;

/**
 * The ServiceDirectory Tool to operate ServiceInstance in the remote Directory Server.
 */
public class ServiceDirectoryClient {

    private static Options options = new Options();

    static {
        Option help = Option.builder("h").longOpt("help").desc("print this message").
                build(); //boolean option
        Option server = Option.builder("s").longOpt("server").required(false).numberOfArgs(1).desc(
                "the server address and port").argName("host:port").build();
        Option command = Option.builder("exec").longOpt("command").required(true).numberOfArgs(1)
                .desc("the command and arguments to execute")
                .build();
        options.addOption(help);
        options.addOption(server);
        options.addOption(command);
    }

    /**
     * The commands Map. LinkedHashMap to preserve the insertion order.
     */
    private static Map<CMD, String> commands = new LinkedHashMap<>();

    enum CMD {
        getAllServices(0),
        getInstanceOf(1),
        getAllInstancesOf(1),
        testConnection(0);
        private final int sizeOfArgs;

        CMD(int sizeOfArgs) {
            this.sizeOfArgs = sizeOfArgs;
        }

        public int getSizeOfArgs() {
            return sizeOfArgs;
        }
    }

    /**
     * The static initializer for commands
     */
    static {
        commands.put(CMD.getAllServices, "");
        commands.put(CMD.getInstanceOf, "<serviceName>");
        commands.put(CMD.getAllInstancesOf, "<serviceName>");
        commands.put(CMD.testConnection, "");
    }

    /**
     * Main function.
     *
     * @param args arguments.
     */
    public static void main(String[] args) {
        ServiceDirectoryClient client = new ServiceDirectoryClient();
        client.runArgs(args);
    }

    /**
     * Run with arguments.
     *
     * @param args arguments.
     */
    public void runArgs(String[] args) {
        CommandLineParser clParser = new DefaultParser();
        CommandLine line;
        String host;
        String port;
        CMD cmd;
        String[] cmdArgs;
        try {
            // parse the command line arguments
            line = clParser.parse(options, args);
            if (line.hasOption("help")) {
                printUsage();
            }
            if (line.hasOption("server")) {
                String server = line.getOptionValue("server");
                try {
                    host = server.split(":")[0];
                    port = server.split(":")[1];
                    System.out.printf("server %s \n", server);
                }catch (Exception e){
                    throw new ParseException(String.format("wrong -server option %S\n",server));
                }
                ServiceDirectory.getServiceDirectoryConfig().setProperty(
                        DirectoryServiceRestfulClient.SD_API_SD_SERVER_FQDN_PROPERTY,
                        host);
                ServiceDirectory.getServiceDirectoryConfig().setProperty(
                        DirectoryServiceRestfulClient.SD_API_SD_SERVER_PORT_PROPERTY,
                        port);
            }
            if (line.hasOption("exec")) {
                String commandString = line.getOptionValue("exec");
                try {
                    cmdArgs = parseCommand(commandString);
                    cmd = CMD.valueOf(cmdArgs[0]);
                    if (cmd.sizeOfArgs + 1 != cmdArgs.length) {
                        throw new ParseException(
                                String.format("Wrong arguments number for command %s, required %s but %s \n",
                                        cmdArgs[0], cmd.sizeOfArgs, cmdArgs.length - 1));
                    }
                    switch (cmd) {
                        case getInstanceOf:
                            lookupInstance(cmdArgs);
                            break;
                        case getAllInstancesOf:
                            lookupInstances(cmdArgs);
                            break;
                        case testConnection:
                            connectToServer();
                            break;
                        default:
                            fail("command " + cmd + " is unsupported now");
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    throw new ParseException(String.format("Unknown command [ %s ] \n", commandString));
                }
            }
        } catch (ParseException exp) {
            // oops, something went wrong
            System.out.println(exp.getMessage());
            printUsage();
        }
    }

    private static String[] parseCommand(String cmdString) {
        StringTokenizer cmdTokens = new StringTokenizer(cmdString, " ");
        String[] args = new String[cmdTokens.countTokens()];
        int tokenIndex = 0;
        while (cmdTokens.hasMoreTokens()) {
            args[tokenIndex] = cmdTokens.nextToken();
            tokenIndex++;
        }
        return args;
    }

    private static void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        formatter.printHelp("SDClient", options);
        for (Map.Entry<CMD, String> cmd : commands.entrySet()) {
            System.out.println("\t" + cmd.getKey() + " \t" + cmd.getValue());
        }
    }

    /**
     * Do the connect command.
     */
    private void connectToServer() {
        try {
            new DirectoryServiceRestfulClient().getAllInstances();
        } catch (ServiceException e) {
            fail(e.getServiceDirectoryError().getErrorMessage());
        }
    }

    /**
     * Do the lookupInstance command.
     *
     * @return true if complete.
     */
    private void lookupInstance(String args[]) {

        String serviceName = args[1].trim();
        System.out.println("lookupInstance, serviceName=" + serviceName);
        try {
            ServiceInstance instance = ServiceDirectory.getLookupManager().lookupInstance(serviceName);
            printServiceInstance(instance);
        } catch (ServiceException e) {
            fail(e.getServiceDirectoryError().getErrorMessage());
        }
    }

    /**
     * Do the lookupInstances command.
     *
     * @return true if complete.
     */
    private void lookupInstances(String args[]) {
        String serviceName = args[1].trim();
        System.out.println("lookupInstances, serviceName=" + serviceName);
        try {
            List<ServiceInstance> instances = ServiceDirectory.getLookupManager().lookupInstances(serviceName);
            if (instances.size() == 0) {
                print("");
            } else {
                for (ServiceInstance instance : instances) {
                    printServiceInstance(instance);
                }
            }
        } catch (ServiceException e) {
            fail(e.getServiceDirectoryError().getErrorMessage());
        }
    }

    private static void fail(String msg) {
        System.out.println("Fail - " + msg);
    }

    private void print(String msg) {
        System.out.println(msg);
    }

    /**
     * Print the ServiceInstance to console.
     *
     * @param instance the ServiceInstance.
     */
    private void printServiceInstance(ServiceInstance instance) {
        print("\nServiceInstance\n-------------------------");
        if (instance == null) {
            print("null");
            return;
        }
        print("serviceName:  " + instance.getServiceName());
        print("status:  " + instance.getStatus());
        print("uri:  " + instance.getUri());
        print("address:  " + instance.getAddress());
        print("monitorEnabled:  " + instance.isMonitorEnabled());
        print("metadata:");
        Map<String, String> meta = instance.getMetadata();
        for (Entry<String, String> entry : meta.entrySet()) {
            print("  " + entry.getKey() + "  =>  " + entry.getValue());
        }
    }

}
