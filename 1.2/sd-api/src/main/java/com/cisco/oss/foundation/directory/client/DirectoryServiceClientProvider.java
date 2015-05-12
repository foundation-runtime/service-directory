package com.cisco.oss.foundation.directory.client;

/**
 * The Provider of DirectoryServiceClient, the ServiceDirectory Manager Factory will provide client
 * by using the provider, When the provider is set up by using {@code setClientProvider()}
 * @since 1.2
 * @see com.cisco.oss.foundation.directory.impl.ConfigurableServiceDirectoryManagerFactory#setClientProvider(DirectoryServiceClientProvider)
 */
public interface DirectoryServiceClientProvider {
    DirectoryServiceClient getClient();
}
