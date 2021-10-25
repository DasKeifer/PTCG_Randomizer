package config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class ConfigUtils
{
    public static File copyFileFromConfigsIfNotPresent(String fileName, String destinationFolderPath) throws IOException
    {
    	String fileSeparator = System.getProperty("file.separator");
        File file = new File(destinationFolderPath + fileSeparator + fileName);
 
        // If the file doesn't exist, we need to create it
        if (!file.exists())
        {
        	file.getParentFile().mkdir();
            file.createNewFile();
            InputStream fileIn = ConfigUtils.class.getResourceAsStream("/config/defaultFiles/" + fileName);
            OutputStream fileOut = new FileOutputStream(file);

            byte[] readBuffer = new byte[2048];
            int lengthToRead = 0;
            while ((lengthToRead = fileIn.read(readBuffer)) != -1)
            {
                fileOut.write(readBuffer, 0, lengthToRead);
            }

            fileIn.close();
            fileOut.close();
        }
        
        return file;
    }
    
    public static String getJarRootPath() throws UnsupportedEncodingException 
    {
        URL url = ConfigUtils.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
        String parentPath = new File(jarPath).getParentFile().getPath();
        return parentPath;
     }
}
