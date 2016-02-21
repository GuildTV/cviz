package cviz;

public interface IProcessor extends Runnable {
    int getChannelNumber();

    void receivedCue();

    void receiveVideoFrame(int layer, long frame, long totalFrames);

    boolean isRunning();
    void stop();
    void kill();
}
