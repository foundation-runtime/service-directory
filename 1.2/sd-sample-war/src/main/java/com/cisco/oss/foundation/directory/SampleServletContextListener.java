/*
 * Copyright 2009-2013, Cisco Systems Inc.
 */
package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.HashSet;
import java.util.Set;


/**
 * Context Listener that show sample to register  when the
 * servlet contain is started. and un-register when shutdown.
 *
 */
public class SampleServletContextListener implements ServletContextListener {

    private final Set<ProvidedServiceInstance> providerInstances = new HashSet<>();

    public SampleServletContextListener() throws Exception {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing Sample Servlet");
        initProvidedServiceInstance(sce.getServletContext());
        try {
            registerEndpoints();
            System.out.println("Sample Servlet fully initialized and all endpoints are registered.");
        } catch (Exception e) {
            System.out.printf("Sample Service failed to initialize with error %s", e.getMessage());
            contextDestroyed(sce);
        } catch (Throwable t) {
            System.out.printf("Sample Service failed to initialize with error %s", t.getMessage());
            throw new RuntimeException();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Shutdown Sample Servlet");
        unregisterEndpoints();
    }

    private void initProvidedServiceInstance(ServletContext servletContext){
        String sdServer = servletContext.getInitParameter("SD_SERVER");
        if (StringUtils.isEmpty(sdServer)){
            sdServer = DirectoryServiceRestfulClient.SD_API_SD_SERVER_FQDN_DEFAULT;
            System.out.println(String.format("The SD_SERVER not set or not set correct, " +
                    "use default value [%s]",sdServer));
        }
        ServiceDirectory.getServiceDirectoryConfig().setProperty(
                DirectoryServiceRestfulClient.SD_API_SD_SERVER_FQDN_PROPERTY, sdServer) ;

        Integer sdPort = DirectoryServiceRestfulClient.SD_API_SD_SERVER_PORT_DEFAULT;
        try {
            sdPort = Integer.parseInt(servletContext.getInitParameter("SD_PORT"));
        }catch (NumberFormatException e){
            System.out.println(String.format("The SD_PORT not set or not set correct, " +
                    "use default value [%s]",sdPort));
        }
        ServiceDirectory.getServiceDirectoryConfig().setProperty(
                DirectoryServiceRestfulClient.SD_API_SD_SERVER_PORT_PROPERTY, sdPort) ;

        String serverName = "sd-sample-war";
        String endpointAddress = "1.1.1.1";
        int port = 12345;
        String url = endpointAddress + ":" + port + "/" + serverName;

        ProvidedServiceInstance inst = new ProvidedServiceInstance(serverName, endpointAddress);
        inst.setUri(url);
        providerInstances.add(inst);
        System.out.println(String.format("Initialized Service Instance [ServiceName = %s, URI = %s]",
                inst.getServiceName(), inst.getUri()));

        System.out.println(String.format("The targeted SD server [server = %s, port = %s]",
                sdServer, sdPort));

    }
    public void registerEndpoints() {

        System.out.println("Try to register Endpoints with the Service Directory API");

        RegistrationManager registrationManager = ServiceDirectory.getRegistrationManager();

        for (ProvidedServiceInstance inst : providerInstances) {
            inst.setStatus(OperationalStatus.UP);
            registrationManager.registerService(inst);
            System.out.printf("Register LCS Provided-service Instance: ServiceName = %s, URI = %s to SD server ",
                    inst.getServiceName(), inst.getUri());
        }
        System.out.printf("Registered %s endpoints with the Service Directory API", providerInstances.size());
    }

    public void unregisterEndpoints() {

        RegistrationManager registrationManager = ServiceDirectory.getRegistrationManager();
        for (ProvidedServiceInstance inst:providerInstances){
            registrationManager.unregisterService(inst.getServiceName(), inst.getAddress());
            System.out.printf("Un-register Provided-service Instance %s, %s from SD server ",
                    inst.getServiceName(), inst.getUri());
        }
        System.out.printf("UnRegistered %s endpoints from the Service Directory API", providerInstances.size());
    }
}