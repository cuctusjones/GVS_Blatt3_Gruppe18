package gvs.distributed;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

public class Submitter {
    public static void main(String[] args) throws Exception
    {
        //  Prepare our context and socket
        try (ZContext context = new ZContext()) {

            Socket receiver = context.createSocket(SocketType.PULL);
            receiver.bind("tcp://localhost:5558");

            //  get result
            String result = new String(receiver.recv(0), ZMQ.CHARSET);


            Socket req = context.createSocket(SocketType.REQ);
            req.connect("tcp://gvs.lxd-vs.uni-ulm.de:27349");

            req.send(result);


            byte[] reply = req.recv(0);
            System.out.println("Serverantwort: " + new String(reply));





        }
    }
}
