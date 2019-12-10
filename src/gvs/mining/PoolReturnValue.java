package gvs.mining;

public class PoolReturnValue {

    public boolean isFound() {
        return found;
    }

    boolean found;

    public String getResult() {
        return result;
    }

    String result;

    public PoolReturnValue(boolean found, String result) {

        this.found = found;
        this.result = result;
    }

}
