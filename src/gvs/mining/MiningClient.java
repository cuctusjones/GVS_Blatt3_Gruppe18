package gvs.mining;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MiningClient {

    public static int numberOfThreads = 16;
    private static ExecutorService pool =  Executors.newFixedThreadPool(numberOfThreads);;

    public static void main(String[] args) {


        try (ZContext context = new ZContext()) {
            Socket sub = context.createSocket(SocketType.SUB);
            sub.connect("tcp://gvs.lxd-vs.uni-ulm.de:27341");
            sub.subscribe(ZMQ.SUBSCRIPTION_ALL);

            Socket req = context.createSocket(SocketType.REQ);
            req.connect("tcp://gvs.lxd-vs.uni-ulm.de:27349");



            String challenge = sub.recvStr();
            System.out.println("Die Herausforderung ist: " + challenge);

            try{


                    boolean found = false;
                    ArrayList<Future<PoolReturnValue>> futures = new ArrayList();
                    for(int i = 0; i < numberOfThreads; i++) {
                        futures.add(pool.submit(new MiningThread(challenge, i, numberOfThreads)));
                    }
                    while(!found) {
                        for(Future<PoolReturnValue> future : futures) {
                            if(future.isDone()) {
                                found = true;
                                System.out.println("Lösung gefunden: " + future.get().getResult());
                                req.send(future.get().getResult().getBytes(), 0);
                                byte[] reply = req.recv(0);
                                System.out.println("Serverantwort: " + new String(reply));
                                break;
                            }
                        }
                    }
                for(Future<PoolReturnValue> future : futures) {
                    future.cancel(true);
                }
                pool.shutdownNow();
            } catch (NoSuchAlgorithmException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            sub.close();
            req.close();



        }
    }
}
