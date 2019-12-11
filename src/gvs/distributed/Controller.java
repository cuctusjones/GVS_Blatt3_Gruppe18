package gvs.distributed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

//
//  Task ventilator in Java
//  Binds PUSH socket to tcp://localhost:5557
//  Sends batch of tasks to workers via that socket
//
public class Controller
{


    public static void main(String[] args) throws Exception
    {
        try (ZContext context = new ZContext()) {

            int connectedWorkers=0;
            int numberOfWorkers = 0;



            //receiver for login and results
            Socket receiver = context.createSocket(SocketType.PULL);
            receiver.bind("tcp://localhost:5558");



            //set number of workers
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Please enter a number of workers you want to connect.");
            numberOfWorkers = Integer.parseInt(reader.readLine());




                System.out.println("Getting new challenge...");
                // receive challenge
                Socket challengeReceiver = context.createSocket(SocketType.SUB);
                challengeReceiver.connect("tcp://gvs.lxd-vs.uni-ulm.de:27341");
                challengeReceiver.subscribe(ZMQ.SUBSCRIPTION_ALL);
                Socket resultSender = context.createSocket(SocketType.REQ);
                resultSender.connect("tcp://gvs.lxd-vs.uni-ulm.de:27349");

                String challenge = challengeReceiver.recvStr();
                System.out.println("new challenge: " + challenge);

                //  Socket to send messages on
                Socket challengeDistributer = context.createSocket(SocketType.PUSH);
                challengeDistributer.bind("tcp://localhost:5557");


                //each worker which connects gets a partition
                while (true) {


                    if(receiver.recvStr().equals("0")){

                        System.out.println("A worker has connected.");
                        connectedWorkers++;
                        challengeDistributer.send(connectedWorkers+","+numberOfWorkers);
                        if(connectedWorkers == numberOfWorkers){
                            break;


                        }

                    }


                }

            challengeDistributer.send(connectedWorkers+","+numberOfWorkers);
            challengeDistributer.send(connectedWorkers+","+numberOfWorkers);
            boolean firstTime = true;
            while(true){

                if(!firstTime){
                    String prevChallenge = challenge;

                    while(prevChallenge.equals(challenge)){
                        challenge = challengeReceiver.recvStr();
                    }

                    System.out.println(challenge);
                }

                //System.out.println("new challenge: " + challenge);
                System.out.println("Sent tasks to workers.");
                challengeDistributer.send(challenge);
                challengeDistributer.send(challenge);


                //  get result
                String result = receiver.recvStr();
                System.out.println("A worker found a solution: "+result);

                resultSender.send(result.getBytes());


                byte[] reply = resultSender.recv(0);
                System.out.println("server response: " + new String(reply));

                challengeDistributer.send("stop");
                firstTime=false;
            }








        }
    }
}
