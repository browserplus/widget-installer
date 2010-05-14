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

public abstract class BootstrapLoader {

	InstallerConfig config;
	
	public abstract String getInstallerUrl();
	public abstract String getDestination();

    public abstract interface ProgressUpdatee {
		public abstract void setPercent(int percent);
    }

	public abstract void loadInstaller(ProgressUpdatee pee)
        throws java.io.IOException, java.lang.InterruptedException;
	

	
    public abstract class InstallerConfig {


		private String installerBaseURL = "http://browserplus.yahoo.com/dist/v2/installer/";
		
		String destinationFileName = "BrowserPlus";
		
		
		public InstallerConfig(Applet applet){
			String installerBaseURL = applet.getParameter("installerBaseURL");
			String destinationFileName = applet.getParameter("destinationFileName");

			this.setBaseUrl(installerBaseURL);
			
			this.setDestination(destinationFileName);			
		}
		

		public abstract String getInstaller();
		public abstract void setInstallerName(String filename);
		
		public String getBaseUrl(){ return installerBaseURL; };
		public void setBaseUrl( String url ){
			if (url == null) return;
			
			if (!url.endsWith("/")) url += "/";
			
			this.installerBaseURL = url;
		}
		
		
		public String getDestination(){ return destinationFileName; };
		public void setDestination( String destinationFileName ){
			if (destinationFileName == null) return;
			
			this.destinationFileName = destinationFileName;
		}
	}
}
