package gvs.distributed;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

public class Sink {
    public static void main(String[] args) throws Exception
    {
        //  Prepare our context and socket
        try (ZContext context = new ZContext()) {
            Socket receiver = context.createSocket(SocketType.PULL);
            receiver.bind("tcp://localhost:5558");

            //  Wait for start of batch
            String string = new String(receiver.recv(0), ZMQ.CHARSET);

            //  Start our clock now
            long tstart = System.currentTimeMillis();

            //  Process 100 confirmations
            int task_nbr;
            int total_msec = 0; //  Total calculated cost in msecs
            for (task_nbr = 0; task_nbr < 100; task_nbr++) {
                string = new String(receiver.recv(0), ZMQ.CHARSET).trim();
                if ((task_nbr / 10) * 10 == task_nbr) {
                    System.out.print(":");
                }
                else {
                    System.out.print(".");
                }
            }

            //  Calculate and report duration of batch
            long tend = System.currentTimeMillis();

            System.out.println(
                    "\nTotal elapsed time: " + (tend - tstart) + " msec"
            );
        }
    }
}
