import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecuteCommand {
    private Runtime runtime = Runtime.getRuntime();
    StringBuilder result = new StringBuilder();

    public StringBuilder getResult() {
        return result;
    }

    public void commands(String cmd){
        result.setLength(0);
        try {
            Process process = runtime.exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String line;
            while ((line = reader.readLine()) !=null){
                result.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
