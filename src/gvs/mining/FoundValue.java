package gvs.mining;

public class FoundValue {

    public boolean isFound() {
        return found;
    }

    boolean found;

    public String getResult() {
        return result;
    }

    String result;

    public FoundValue(boolean found, String result) {

        this.found = found;
        this.result = result;
    }

}
