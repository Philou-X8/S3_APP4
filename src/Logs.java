import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public class Logs {
    static public void AddLogs(String textLine){
        try {
            String logInput = "[ " + new Date().toString() + " ]: " + textLine + "\n";

            Files.write(Paths.get("logs.txt"), logInput.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            try{
                FileWriter logFile = new FileWriter("logs.txt");

                logFile.close();
                System.out.println("log file created");
                AddLogs(textLine);

            } catch (IOException e2) {
                System.out.println("An error occurred.");
                e2.printStackTrace();
            }
        }
    }
}
