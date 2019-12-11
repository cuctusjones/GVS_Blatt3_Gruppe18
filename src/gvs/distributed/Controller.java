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

            System.out.println("Get challenge...");
            // receive challenge
            Socket challengeReceiver = context.createSocket(SocketType.SUB);
            challengeReceiver.connect("tcp://gvs.lxd-vs.uni-ulm.de:27341");
            challengeReceiver.subscribe(ZMQ.SUBSCRIPTION_ALL);
            Socket resultSender = context.createSocket(SocketType.REQ);
            resultSender.connect("tcp://gvs.lxd-vs.uni-ulm.de:27349");

            String challenge = challengeReceiver.recvStr();
            System.out.println("Die Herausforderung ist: " + challenge);

            //  Socket to send messages on
            Socket challengeDistributer = context.createSocket(SocketType.PUSH);
            challengeDistributer.bind("tcp://localhost:5557");


            //each worker which connects gets a partition
            while (connectedWorkers != numberOfWorkers) {


                if(receiver.recvStr().equals("0")){

                    System.out.println("A worker has connected.");

                    challengeDistributer.send(connectedWorkers+","+numberOfWorkers);
                    connectedWorkers++;


                }



            }


            System.out.println("Send tasks to workers.");
            challengeDistributer.send(challenge);


            //  get result
            String result = receiver.recvStr();
            System.out.println("A worker found a solution: "+result);
            resultSender.send(result.getBytes());


            byte[] reply = resultSender.recv(0);
            System.out.println("Serverantwort: " + new String(reply));


            //TODO: terminate all workers





        }
    }
}
