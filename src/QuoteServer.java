import java.io.*;

public class QuoteServer {
    /**
     * Server's main
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new QuoteServerThread().start();
    }
}