package Utilities;

import java.io.Serializable;

import appia.protocols.common.InetWithPort;

public class PeerStatus implements Serializable {

	private static final long serialVersionUID = -8670053468677857471L;
	
	private InetWithPort addr;
	private boolean LP=false;

	public PeerStatus (boolean in,InetWithPort addr_in){
		LP=in;
		addr=addr_in;
	}
	
	public InetWithPort getAddr() {
		return addr;
	}
	public void setAddr(InetWithPort addr) {
		this.addr = addr;
	}
	public boolean isLP() {
		return LP;
	}
	public void setLP(boolean lp) {
		LP = lp;
	}
	
	
}
