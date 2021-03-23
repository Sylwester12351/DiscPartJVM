import lombok.Getter;

import java.io.*;
import java.util.Properties;

public class Config {
    @Getter
    String  mntNTFS;
    @Getter
    String mntOTHER;

    public void openConfigFile(){
        String userHome = System.getProperty("user.home");
        try (InputStream input = new FileInputStream(userHome+"/discpart.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            mntNTFS = prop.getProperty("mountNTFSin");
            mntOTHER = prop.getProperty("mountOTHERin");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            createConfigFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //
    private static void createConfigFile() {
        String userHome = System.getProperty("user.home");
        try {
            OutputStream outputStream = new FileOutputStream(userHome+"/discpart.properties");
            Properties properties = new Properties();
            properties.setProperty("mountNTFSin","/mnt/ntfs");
            properties.setProperty("mountOTHERin","/mnt/other");
            try {
                properties.store(outputStream,"Plik konfiguracyjny programu DiscPartJVM");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
