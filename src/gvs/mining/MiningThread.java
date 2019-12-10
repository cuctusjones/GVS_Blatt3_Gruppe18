package gvs.mining;


import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

public class MiningThread implements Callable {
    public String msg;
    public Util util;
    public int number;
    public int threadCount;

    public MiningThread(String msg, int number, int threadCount) throws NoSuchAlgorithmException {
        this.number = number;
        util = new Util();
        this.msg = msg;
        this.threadCount = threadCount;
    }


    @Override
    public Object call() throws Exception {
        String attempt = msg;

        long i=number;

        while(!util.validate(msg,attempt)) {
            if(Thread.currentThread().isInterrupted()) {
                return new PoolReturnValue(false, attempt);
            }



            attempt = msg+Long.toHexString(i);
            i += threadCount;




        }
        return new PoolReturnValue(true, attempt);

    }
}
