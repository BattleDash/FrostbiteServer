package me.battledash.kyber.streams;

public class BitStreamRollback {

    private BitStreamWrite stream;
    private BitStreamRead rstream;
    private final int position;
    private boolean hasValidPosition;

    public BitStreamRollback(BitStreamWrite stream) {
        this.stream = stream;
        this.position = stream.tell();
        this.hasValidPosition = true;
    }

    public BitStreamRollback(BitStreamRead stream) {
        this.rstream = stream;
        this.position = stream.tell();
        this.hasValidPosition = true;
    }

    public void rollback() {
        if (this.hasValidPosition) {
            this.hasValidPosition = false;
            if (this.stream != null) {
                this.stream.seek(this.position);
            } else {
                this.rstream.seek(this.position);
            }
        }
    }

}
