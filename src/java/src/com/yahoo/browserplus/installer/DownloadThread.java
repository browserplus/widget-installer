package com.yahoo.browserplus.installer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

class DownloadThread implements Runnable 
{
    Thread m_runner;
    String m_address;
    String m_localFileName;
    String m_state;
    int m_percent;
    BootstrapLoader m_loader;

    public static void LOG(String msg) {
        System.out.println( "BP Loader Thread: " + msg );
    }

    public DownloadThread() 
    {
        m_state = "downloading";
        m_percent = 0;
    }

    public String getState() 
    {
        return m_state;
    }

    public int getPercent() 
    {
        return m_percent;
    }
    
    public void start(BootstrapLoader loader, String address, String localFileName) 
    {
        m_loader = loader;
        m_address = address;
        m_localFileName = localFileName;
        
        m_runner = new Thread(this, "DownloadThread");
        // (1) Create a new thread.
        System.out.println(m_runner.getName());
        // (2) Start the thread.
        m_runner.start();
    }
    
    public void run() 
    {
        //Display info about this particular thread
        System.out.println("stupid thread is now running");
        
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;

        m_state = "downloading";                
        LOG("**STATE("+m_state+")**");

        try {
            // Get the URL
            URL url = new URL(m_address);
            // Open an output stream to the destination file on our local filesystem
            out = new BufferedOutputStream(new FileOutputStream(m_localFileName));
            conn = url.openConnection();
            conn.setUseCaches(false);
            
            // Read the data
            int length = 0;
            try {
                length = conn.getContentLength();
            } catch(Exception e) {
                LOG( "Failed to get content length!!  assuming 1.5mb" );
                length = 1024 * 1500;
            }

            LOG( "Reading " + length + " bytes" );

            in = conn.getInputStream();
            
            byte[] buffer = new byte[1024];
            int numRead;
            int totalRead = 0;
            int lastPercent = -1;

            while ((numRead = in.read(buffer)) != -1) {
                totalRead += numRead;
                m_percent = (100 * totalRead) / length;
                if (m_percent != lastPercent && m_percent % 5 == 0) {
                    lastPercent = m_percent;
                    LOG( "Percent Done: " + m_percent );
                }
                out.write(buffer, 0, numRead);
            } 
            m_percent = 100;

            LOG( "File Downloaded Succesfully!" );

            m_state = "downloaded";                
            LOG("**STATE("+m_state+")**");
            
            if (in != null) { in.close(); }
            if (out != null) { out.close(); }

            LOG( "Loading the installer" );

            m_state = "launching";                
            LOG("**STATE("+m_state+")**");
            m_loader.loadInstaller();
            m_state = "complete";                
            LOG("**STATE("+m_state+")**");
            // Done! Just clean up and get out
            
        } catch (java.lang.SecurityException exception) {
            LOG( "INSTALL FAILED!  Security Exception - Permissions maybe?.." );
            exception.printStackTrace();

            m_state = "SecurityException";
            LOG("**STATE("+m_state+")**");
            
        } catch (Exception exception) {
            LOG( "INSTALL FAILED!  Couldn't fetch BrowserPlus installer" );
            exception.printStackTrace();

            // we must now let javascript know!
            m_state = "error";
            LOG("**STATE("+m_state+")**");
        } finally {
            conn = null;
        }
    }
}
