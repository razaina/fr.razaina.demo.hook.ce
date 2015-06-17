package fr.razaina.demohookce;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class RootHelpers {

	public static String BinaryName = "";
	public static String BinaryPath = "";
	private static final String TAG = RootHelpers.class.toString();

	public static int getPID(String processName)
	{
		String[] cmds = {"pidof " + processName + "\n"};
		try {
			return Integer.parseInt(runAsRoot(cmds));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}
	/**
	 * <p>
	 * This function is useful for dropping a binary file somewhere according to the path passed
	 * as a parameter. While it is dropped its access permission is set with the chmod command.s
	 * </p>
	 * 
	 * @param binary You must provide the binary file you want to drop as an InputStream
	 * @param path This is the path where you want to drop your binary file
	 * @param binaryName Specify the name of your dropped binary
	 */
	public static String dropBinary(InputStream binary, String path, String binaryName)
	{ 
		FileOutputStream out; 
		byte[] buff = new byte[1024];
		int read = 0;

		try {
			File hijack = new File(path, binaryName);
			System.out.println("> Dropping binary " + binaryName + " at " + path + "..." );
			out = new FileOutputStream(hijack);
			while ((read = binary.read(buff)) > 0) {
				out.write(buff, 0, read);
			}
			binary.close();
			out.close();
			System.out.println("> Done!");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		String[] cmds = {"chmod 777 " + path + binaryName + "\n"};

		try { 
			BinaryName = binaryName;
			BinaryPath = path;
			return runAsRoot(cmds);

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace(); 
		}
		return "NULL";
	}

	/**
	 * <p>
	 * This function can be used to run some commands as root.
	 * Commands are passed through an array of string.
	 * A shell is spawned with root rights (su) and all the commands are ran through this shell.
	 * </p>
	 * 
	 * @param cmds Commands that are passed through an array of strings
	 * @return Return the output of the command
	 * @throws Exception
	 */
	public static String runAsRoot(String[] cmds) throws Exception {
		String output = "";
		Process su = Runtime.getRuntime().exec("su");

		DataOutputStream os = new DataOutputStream(su.getOutputStream()); 
		for (String tmpCmd : cmds) {
			System.out.println("\n> Running command: " + tmpCmd);
			os.writeBytes(tmpCmd+"\n");
			os.flush();
		}        
		os.writeBytes("exit\n");
		os.flush();



		BufferedReader reader = 
				new BufferedReader(new InputStreamReader(su.getInputStream()));

		String line = "";			
		while ((line = reader.readLine())!= null) {
			output = output + line + "\n";
		} 
		System.out.println(">Output ==> " + output);
		return output;
	}

	public static Process runAsRootAsync(String []cmds, final Task.OnMessageListener listener) throws IOException{

		final Process su = Runtime.getRuntime().exec("su");

		DataOutputStream os = new DataOutputStream(su.getOutputStream()); 
		for (String tmpCmd : cmds) {
			System.out.println("\n> Running command: " + tmpCmd);
			os.writeBytes(tmpCmd+"\n");
			os.flush();
		}        
		os.writeBytes("exit\n");
		os.flush();


		if(listener!=null){
			Log.i(TAG,"Spawning a new Listener Thread");
			final BufferedReader reader = new BufferedReader(new InputStreamReader(su.getInputStream()));
			//Spawn a thread that do reads and send the output to our listener!
			new Thread(new Runnable() {
				Process mProcess = su;
				@Override
				public void run() {
					Log.i(TAG,"Reader thread started");
					android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
					String line="";
					try {
						while ((line = reader.readLine())!= null) {
							Log.i(TAG,line);
							listener.onMessage(line);
						}
						Log.i(TAG,"Reader thread exiting:"+reader);
					} catch (IOException e) {
						Log.i(TAG,"Error reading output:"+e.toString());
					} 
				}				
			}).start();		   	
		}else
			Log.i(TAG,"No listener");
		
		return su;
	}

	/**
	 * Useful command for copying a file from a location to another one
	 * 
	 * @param src Source
	 * @param dst Destination
	 * @throws java.io.IOException
	 */
	public static void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}


}
