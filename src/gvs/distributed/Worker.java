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

    public static int numberOfThreads = 12; //
    private static ExecutorService pool =  Executors.newFixedThreadPool(numberOfThreads);

    public static void main(String[] args) throws Exception {






            try (ZContext context = new ZContext()) {
                //  Socket to receive messages on

                //  Socket to send messages to
                ZMQ.Socket sender = context.createSocket(SocketType.PUSH);
                sender.connect("tcp://localhost:5558");

                //  Socket to receive messages on
                ZMQ.Socket receiver = context.createSocket(SocketType.PULL);
                receiver.connect("tcp://localhost:5557");

                //anmeldung
                sender.send("0");

                int connectedWorkers = 0;
                int numberOfWorkers = 1;
                int id = -1;

                while(connectedWorkers!=numberOfWorkers){
                    String p = receiver.recvStr();
                    if(id==-1){
                        id = Integer.parseInt(p.split(",")[0]) - 1;
                    }
                    connectedWorkers=Integer.parseInt(p.split(",")[0]);
                    numberOfWorkers = Integer.parseInt(p.split(",")[1]);
                    //System.out.println(numberOfWorkers + " connectedWorkers " + connectedWorkers);
                }

                while(true) {

                    String challenge = "empty";
                    while (challenge.equals("empty")) {
                        //System.out.println("setting new challenge");
                        challenge = receiver.recvStr();

                        if (challenge.split(",").length > 1 || challenge.equals("stop")) {
                            challenge = "empty";
                        }
                    }

                System.out.println("Searching for solution for challenge " + challenge + "...");
                    try {

                        boolean found = false;
                        ArrayList<Future<FoundValue>> futures = new ArrayList();
                        for (int i = 0; i < numberOfThreads; i++) {
                            futures.add(
                                pool.submit(new MiningThread(challenge, i + numberOfThreads * id,
                                    numberOfThreads * numberOfWorkers)));
                        }
                        while (!found) {
                            if(receiver.recvStr(ZMQ.NOBLOCK) != null ){
                                //System.out.println("other worker found solution");
                                break;
                            }

                            for (Future<FoundValue> future : futures) {
                                if (future.isDone()) {
                                    found = true;
                                    System.out
                                        .println("solution found: " + future.get().getResult());
                                    sender.send(future.get().getResult().getBytes(), 0);
                                    //byte[] reply = req.recv(0);
                                    //System.out.println("Serverantwort: " + new String(reply));
                                    break;
                                }
                            }
                        }
                        for (Future<FoundValue> future : futures) {
                            future.cancel(true);
                        }
                        //pool.shutdownNow(); // out of loop ?
                    } catch (NoSuchAlgorithmException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }



        }
    }
}