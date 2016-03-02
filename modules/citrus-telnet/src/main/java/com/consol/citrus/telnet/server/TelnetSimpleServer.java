package com.consol.citrus.telnet.server;

/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.telnet.client.TelnetClient;

/***
 * Simple TCP server.
 * Waits for connections on a TCP port in a separate thread.
 *
 * @author Magnus Karlsson
 * @since 2.6
 ***/
public class TelnetSimpleServer implements Runnable
{

    /** Logger */
	private static Logger log = LoggerFactory.getLogger(TelnetClient.class);
	
	ServerSocket serverSocket = null;
    Socket clientSocket = null;
    Thread listener = null;

    /***
     * test of client-driven sub-negotiation.
     *
     * @param port - server port on which to listen.
     ***/
    public TelnetSimpleServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);

        listener = new Thread (this);

        listener.start();
    }

    /***
     * Run for the thread. Waits for new connections
     ***/
    public void run()
    {
        boolean bError = false;
        while(!bError)
        {
            try
            {
                clientSocket = serverSocket.accept();
                synchronized (clientSocket)
                {
                    try
                    {
                    	clientSocket.wait();
                    }
                    catch (Exception e)
                    {
                        System.err.println("Exception in wait, "+ e.getMessage());
                    }
                    try
                    {
                    	log.info("--------------- OUTPUT 1");
                    }
                    catch (Exception e)
                    {
                        System.err.println("Exception in close, "+ e.getMessage());
                    }
                }
            }
            catch (IOException e)
            {
                bError = true;
            }
        }

        try
        {
            serverSocket.close();
        }
        catch (Exception e)
        {
            System.err.println("Exception in close, "+ e.getMessage());
        }
    }


    /***
     * Disconnects the client socket
     ***/
    public void disconnect()
    {
        synchronized (clientSocket)
        {
            try
            {
                clientSocket.notify();
            }
            catch (Exception e)
            {
                System.err.println("Exception in notify, "+ e.getMessage());
            }
        }
    }

    /***
     * Stop the listener thread
     ***/
    public void stop()
    {
        listener.interrupt();
        try
        {
            serverSocket.close();
        }
        catch (Exception e)
        {
            System.err.println("Exception in close, "+ e.getMessage());
        }
    }

    /***
     * Gets the input stream for the client socket
     ***/
    public InputStream getInputStream() throws IOException
    {
        if(clientSocket != null)
        {
            return(clientSocket.getInputStream());
            
        }
        else
        {
        	 return(null);
        }
    }

    /***
     * Gets the output stream for the client socket
     ***/
    public OutputStream getOutputStream() throws IOException
    {
        if(clientSocket != null)
        {
        	return(clientSocket.getOutputStream());
            
        }
        else
        {
        	return(null);
        }
    }
}