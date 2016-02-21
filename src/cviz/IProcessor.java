package cviz;

public interface IProcessor {
    int getChannelNumber();

    void receivedCue();

    void receiveVideoFrame(int layer, long frame, long totalFrames);
}
