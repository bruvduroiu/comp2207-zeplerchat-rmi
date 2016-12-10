import com.bogdanbuduroiu.zeplerchat.client.controller.Client;
import com.bogdanbuduroiu.zeplerchat.server.controller.RegistryServer;

import java.util.concurrent.ExecutionException;

/**
 * Created by bogdanbuduroiu on 10/12/2016.
 */
public class Main {

    public static void main(String[] args) {
        try {
            new Thread(new RegistryServer()).start();
            new Client();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
