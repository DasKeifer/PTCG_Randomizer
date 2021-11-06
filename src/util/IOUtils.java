package util;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import config.ConfigConstants;

public class IOUtils 
{    
	public static final String NEWLINE = System.getProperty("line.separator");
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	private IOUtils() {}
	
	public static void showScrollingMessageDialog(
			Component parentComponent,
            String message,
            String title,
            int messageType
    ) 
	{
		showScrollingMessageDialog(parentComponent, message, title, messageType, 10, 50);
	}
	
	public static void showScrollingMessageDialog(
			Component parentComponent,
            String message,
            String title,
            int messageType,
            int heightLines,
            int widthChars
    ) 
	{
		// Make the text area but make it wrap so it looks nicer
		JTextArea textArea = new JTextArea(message, heightLines, widthChars);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setOpaque(false);
		
	    JScrollPane scrollPane = new JScrollPane(textArea,
	    		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
	    		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	    JOptionPane.showMessageDialog(parentComponent, scrollPane, title, messageType);
	}
	
    public static String getJarRootPath() throws UnsupportedEncodingException 
    {
        URL url = ConfigConstants.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
        return new File(jarPath).getParentFile().getPath();
    }

    public static File copyFileFromConfigsIfNotPresent(String resourcePath, String fileName, String destinationFolderPath) throws IOException
    {
        File file = new File(destinationFolderPath + FILE_SEPARATOR + fileName);
 
        // If the file doesn't exist, we need to create it
        file.getParentFile().mkdir();
        if (file.createNewFile()) // returns false if the file already exists
        {
            try (InputStream fileIn = ConfigConstants.class.getResourceAsStream(resourcePath + fileName);
            		OutputStream fileOut = new FileOutputStream(file)
			)
            {
		        byte[] readBuffer = new byte[2048];
		        int lengthToRead = 0;
		        while ((lengthToRead = fileIn.read(readBuffer)) != -1)
		        {
		            fileOut.write(readBuffer, 0, lengthToRead);
		        }
            }
            // Let any errors propagate
        }
        
        return file;
    }
}
