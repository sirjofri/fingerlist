package de.sirjofri.fingerlist;
import java.net.*;
import java.io.*;
import android.app.*;

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
				try{
					String[] parts = address.split("@");
					String hostpart;
					String req;
					String host;
					int port;
					if (parts.length == 1) { // whois
						req = "";
						hostpart = parts[0];
					} else if (parts.length == 2) { // user
						req = parts[0];
						hostpart = parts[1];
					} else {
						throw new Exception("Invalid string");
					}
					parts = hostpart.split(":");
					if (parts.length == 1) {
						port = 79;
						host = parts[0];
					} else if (parts.length == 2) {
						port = Integer.parseInt(parts[1]);
						host = parts[0];
					} else {
						throw new Exception("Invalid string");
					}
					
					Socket sock = new Socket(host, port);
					PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					out.println(req);
					content = new String();
					int character;
					while ((character = in.read()) != -1)
						content += (char)character;
					sock.close();
				} catch(UnknownHostException e)
				{
					content = "Host not found: " + e.getMessage();
				} catch(IOException e)
				{
					content = "Unable to read/write: " + e.getMessage();
				} catch(Exception e)
				{
					content = "Error: " + e.getMessage();
				}
				activity.notifyDataChanged();
			}
		};
		t.start();
	}
}
