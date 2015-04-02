package com.cisco.oss.foundation.directory.client;

/**
 * The Provider of DirectoryServiceClient, the ServiceDirectory will provide client
 * by using the provider, When the provider is set up by using {@code ServiceDirectory.setClientProvider()}
 * @since 1.2
 * @see com.cisco.oss.foundation.directory.ServiceDirectory#setClientProvider(DirectoryServiceClientProvider)
 */
public interface DirectoryServiceClientProvider {
    DirectoryServiceClient getClient();
}
