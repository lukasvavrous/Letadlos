package utils;

public class FpsHelper {
    private static FpsHelper instance;

    private long lastFrame;

    private int deltaMs;

    private FpsHelper(){ }

    public static FpsHelper getInstance(){
        if (instance == null)
            instance = new FpsHelper();

        return instance;
    }

    public void nextFrame(){
        long time = System.currentTimeMillis();
        deltaMs = (int) (time - lastFrame);

        lastFrame = time;
    }

    public int getDeltaMs(){
        return deltaMs;
    }
    public int getFps(){
        if (deltaMs <= 0)
            return Integer.MAX_VALUE;

        return 1000 / deltaMs;
    }
}
