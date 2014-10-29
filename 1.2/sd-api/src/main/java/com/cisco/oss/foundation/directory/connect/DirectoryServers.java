package com.cisco.oss.foundation.directory.connect;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class DirectoryServers {
	
//	private final static Logger LOGGER = LoggerFactory.getLogger(DirectoryServers.class);
	
	private List<InetSocketAddress> servers ;
	
	private int index = -1;

	public DirectoryServers(){
		servers = new ArrayList<InetSocketAddress>();
		servers.add(InetSocketAddress.createUnresolved("vcsdirsvc", 2013));
	}
	
	public DirectoryServers(List<String> servers){
		parseServers(servers);
		if(servers.size() == 0){
			throw new IllegalArgumentException("Servers list is invalid.");
		}
	}
	
	public void setServers(List<String> servers){
		
		parseServers(servers);
		if(servers.size() == 0){
			throw new IllegalArgumentException("Servers list is invalid.");
		}
	}
	
	public InetSocketAddress getNextDirectoryServer(){
		
		index = index + 1;
		if(index == servers.size()){
			index = 0;
		}
		InetSocketAddress unresolved = servers.get(index);
		
//		List<InetSocketAddress> resolved = null;
//		try {
//			InetAddress[] addresses = InetAddress.getAllByName(unresolved.getHostName());
//			for(InetAddress address : addresses){
//				if(resolved == null){
//					resolved = new ArrayList<InetSocketAddress>();
//				}
//				resolved.add(new InetSocketAddress(address.getHostAddress(), unresolved.getPort()));
//			}
//		} catch (UnknownHostException e) {
//			LOGGER.error("Resolve the Directory Server failed, host=" + unresolved.getAddress().toString() + " - " + e.getMessage());
//		}
//		
//		if(resolved == null){
//			return Collections.emptyList();
//		}
		return new InetSocketAddress(unresolved.getHostName(), unresolved.getPort());
	}
	
	private void parseServers(List<String> servers){
		if(servers != null && servers.size() > 0){
			this.servers = new ArrayList<InetSocketAddress>();
			for(String server : servers){
				String[] ss = server.split(":");
				if(ss.length == 2){
					String host = ss[0];
					int port = 0;
					try{
						port = Integer.valueOf(ss[1]);
					} catch(NumberFormatException e){
						// do nothing.
					}
					if(validateServer(host, port)){
						this.servers.add(InetSocketAddress.createUnresolved(host, port));
					}
				}
			}
		}
	}
	
	private boolean validateServer(String host, int port){
		if(host==null || host.isEmpty()){
			return false;
		}
		if(port <=0 || port > 65535){
			return false;
		}
		return true;
	}
	
}
