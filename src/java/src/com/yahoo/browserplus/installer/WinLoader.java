/*
 * Copyright 2010, Yahoo!
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *  3. Neither the name of Yahoo! nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *   SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 *  IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package com.yahoo.browserplus.installer;

import java.applet.Applet;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.NumberFormat;

public class WinLoader extends BootstrapLoader{
    private String m_destination;
    
	public WinLoader(Applet applet) throws Exception{
		super();
		
		config = new WinInstallerConfig( applet );
        m_destination = System.getProperty("java.io.tmpdir") +
                File.separator + config.getDestination();
         
		bplusloader.LOG( "Win Loader created" );
	}
	
	public String getInstallerUrl(){
		String url = config.getInstaller();
		bplusloader.LOG( "Installer Url: " +  url );
		return url;
	}

	private String m_destiation;
    
	public String getDestination(){
		bplusloader.LOG( "Got destination: " +  m_destination );
		return m_destination;
	}
	
	public void loadInstaller(BootstrapLoader.ProgressUpdatee pee)
        throws java.io.IOException, java.lang.InterruptedException
    {
        pee.setPercent(0);

        Runtime rt = Runtime.getRuntime();
        String installer = getDestination();
        bplusloader.LOG( "Executing installer: " +  installer );
        String[] openCommand = {
            installer,
            "-nogui=true",
            "-verbose=true"
        };
        Process installerProc = rt.exec(openCommand);

        pee.setPercent(5);

        // collect installer progress
        {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(installerProc.getInputStream(), "UTF-8"));
            String line;
            int lastPercent = -1;
            int percent;
            
            while ((line = reader.readLine()) != null) {
                bplusloader.LOG( "read from installer: " + line );
                try {
                    percent = NumberFormat.getIntegerInstance().parse(line).intValue();
                } catch (java.text.ParseException pe) {
                    percent = 0;
                }
                
                if (percent >=0 && percent <= 100 &&
                    percent > lastPercent)
                {
                    lastPercent = percent;
                    pee.setPercent((int) ((percent / 100.0) * 75.0) + 15);
                }
            }
        }

        installerProc.waitFor();

        pee.setPercent(95);

        // now delete the file
        new File(installer).delete();

        pee.setPercent(100);
	}
	
	class WinInstallerConfig extends BootstrapLoader.InstallerConfig{

		private String installerWin = "win32";
	
		public WinInstallerConfig(Applet applet){
			super(applet);

			if (!this.destinationFileName.toLowerCase().endsWith(".exe")){
				this.destinationFileName += ".exe";
			}
			
			String installerName = applet.getParameter("installerWin");
			this.setInstallerName(installerName);
		}
		
		public String getInstaller(){ return getBaseUrl() + installerWin; };
		
		public void setInstallerName( String winFileName ){
			if (winFileName != null)
				this.installerWin = winFileName;
		}
	}
}
