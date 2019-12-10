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

            int workerID;
            //  Socket to receive messages on
            ZMQ.Socket receiver = context.createSocket(SocketType.PULL);
            receiver.connect("tcp://localhost:5557");

            String challenge = receiver.recvStr();

            try{


                boolean found = false;
                ArrayList<Future<FoundValue>> futures = new ArrayList();
                for(int i = 0; i < numberOfThreads; i++) {
                    futures.add(pool.submit(new MiningThread(challenge, i, numberOfThreads)));
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








            //  Process tasks forever
            /*while (!Thread.currentThread().isInterrupted()) {
                String string = new String(receiver.recv(0), ZMQ.CHARSET).trim();
                long msec = Long.parseLong(string);
                //  Simple progress indicator for the viewer
                System.out.flush();
                System.out.print(string + '.');

                //  Do the work
                Thread.sleep(msec);

                //  Send results to sink
                sender.send(ZMQ.MESSAGE_SEPARATOR, 0);
            }*/
        }
    }
}