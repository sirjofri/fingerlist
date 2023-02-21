package de.sirjofri.fingerlist;
import java.net.*;
import java.io.*;
//import android.app.*;
import android.net.Uri;

//import androidx.annotation.NonNull;

public class FingerEntry
{
	private String address;
	String content;
	FingerAdapter adapter;
	MainActivity activity;
	
	public FingerEntry(String address, FingerAdapter a, MainActivity ac)
	{
		this.address = address;
		this.content = "";
		this.adapter = a;
		this.activity = ac;
		load();
	}
	
	public void setAddress(String a)
	{
		address = a;
		load();
	}
	public String getAddress()
	{
		return address;
	}

	@Override
	public String toString()
	{
		return address + ": " + content;
	}
	
	public void load()
	{
		Thread t = new Thread() {
			@Override
			public void run()
			{
				content = "Loading";
				activity.notifyDataChanged();
				String req;
				String host;
				int port;
				Uri uri;
				if(address.contains("//")) { //it is likely a URL. finger URLs without // would default to whatever host makes sense as default.
					uri=Uri.parse(address);
					port = uri.getPort();
					if(port == -1) {
						port = 79;
					}
					host = uri.getHost();
					String path;
					path = uri.getPath();
					req = path.substring(1); // skip leading /s that URIs will have.
				} else {
					String[] parts = address.split("@");
					String hostpart;
					if (parts.length == 1) { // whois
						req = "";
						hostpart = parts[0];
					} else if (parts.length == 2) { // user
						req = parts[0];
						hostpart = parts[1];
					} else {
						content = "too many @s";
						activity.notifyDataChanged();
						return;
					}
					parts = hostpart.split(":");
					if (parts.length == 1) {
						port = 79;
						host = parts[0];
					} else if (parts.length == 2) {
						port = Integer.parseInt(parts[1]);
						host = parts[0];
					} else {
						content = "too many :s in hostpart";
						activity.notifyDataChanged();
						return;
					}
				}
				// if we got here, we're all good. a host and port are set, and a req.
				try {
					Socket sock;
					sock = new Socket(host, port);
					PrintWriter out = new PrintWriter( sock.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					out.print(req + "\r\n");
					out.flush();
					content = "";
					int character;
					while ((character = in.read()) != -1) {
						content += (char) character;
					}
					sock.close();
				} catch(UnknownHostException e) {
					content = "UnknownHost";
					activity.notifyDataChanged();
					return;
				} catch(java.io.IOException e) {
					content = "IOException";
					activity.notifyDataChanged();
					return;
				}
				activity.notifyDataChanged();
			}
		};
		t.start();
	}
}
