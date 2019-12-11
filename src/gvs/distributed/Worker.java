package gvs.distributed;


import static gvs.mining.MiningClient.numberOfThreads;

import gvs.mining.FoundValue;
import gvs.mining.MiningThread;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

//
//  Task worker in Java
//  Connects PULL socket to tcp://localhost:5557
//  Collects workloads from ventilator via that socket
//  Connects PUSH socket to tcp://localhost:5558
//  Sends results to sink via that socket
//
public class Worker {

    public static int numberOfThreads = 16;
    private static ExecutorService pool =  Executors.newFixedThreadPool(numberOfThreads);

    public static void main(String[] args) throws Exception {
        try (ZContext context = new ZContext()) {
            //  Socket to receive messages on


            //  Socket to send messages to
            ZMQ.Socket sender = context.createSocket(SocketType.PUSH);
            sender.connect("tcp://localhost:5558");


            //anmeldung
            sender.send("0");


            //  Socket to receive messages on
            ZMQ.Socket receiver = context.createSocket(SocketType.PULL);
            receiver.connect("tcp://localhost:5557");

            String p = receiver.recvStr();

            int id = Integer.parseInt(p.split(",")[0]);
            int numberOfWorkers = Integer.parseInt(p.split(",")[1]);

            String challenge = receiver.recvStr();
            System.out.println("Searching for solution for challenge " + challenge + "...");
            try{


                boolean found = false;
                ArrayList<Future<FoundValue>> futures = new ArrayList();
                for(int i = 0; i < numberOfThreads; i++) {
                    futures.add(pool.submit(new MiningThread(challenge, i+numberOfThreads*id, numberOfThreads*numberOfWorkers)));
                }
                while(!found) {
                    for(Future<FoundValue> future : futures) {
                        if(future.isDone()) {
                            found = true;
                            System.out.println("Lösung gefunden: " + future.get().getResult());
                            sender.send(future.get().getResult().getBytes(), 0);
                            //byte[] reply = req.recv(0);
                            //System.out.println("Serverantwort: " + new String(reply));
                            break;
                        }
                    }
                }
                for(Future<FoundValue> future : futures) {
                    future.cancel(true);
                }
                pool.shutdownNow();
            } catch (NoSuchAlgorithmException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }


            System.out.println("Closing worker.");


        }
    }
}