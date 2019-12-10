package gvs.distributed;

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

            int numberOfWorkers=0;

            //macht keinen sinn oder ? wir haben ja keinen bezug zu den workern
            //wie schicken wir etwas an nen bestimmten worker, va aufm localhost verschiedene ports ?
            //ArrayList<Worker> workers= new ArrayList<>();

            //receiver for login and results
            Socket receiver = context.createSocket(SocketType.PULL);
            receiver.bind("tcp://localhost:5558");

            while(true){
                if(receiver.recvStr().equals("0")){
                    numberOfWorkers++;
                }


                break;
            }


            // receive challenge
            Socket challengeReceiver = context.createSocket(SocketType.SUB);
            challengeReceiver.connect("tcp://gvs.lxd-vs.uni-ulm.de:27341");
            challengeReceiver.subscribe(ZMQ.SUBSCRIPTION_ALL);

            String challenge = challengeReceiver.recvStr();
            System.out.println("Die Herausforderung ist: " + challenge);




            //  Socket to send messages on
            ZMQ.Socket challengeDistributer = context.createSocket(SocketType.PUSH);
            challengeDistributer.bind("tcp://localhost:5557");
            System.out.println("Sending tasks to workers\n");







            //  get result
            String result = new String(receiver.recv(0), ZMQ.CHARSET);


            Socket resultSender = context.createSocket(SocketType.REQ);
            resultSender.connect("tcp://gvs.lxd-vs.uni-ulm.de:27349");

            resultSender.send(result);


            byte[] reply = resultSender.recv(0);
            System.out.println("Serverantwort: " + new String(reply));








        }
    }
}
