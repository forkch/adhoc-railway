package ch.fork.adhocrailway.railway.brain.brain;

public interface BrainListener {

    void sentMessage(String sentMessage);
    void receivedMessage(String receivedMessage);
    void brainReset(String receivedMessage);

    void brainMessage(String receivedMessage);
}
