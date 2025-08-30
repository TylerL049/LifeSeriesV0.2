package net.mat0u5.lifeseries.seasons.session;

public abstract class SessionAction {
    public boolean hasTriggered = false;
    public int triggerAtTicks;
    public String sessionMessage;
    public String sessionId;

    public SessionAction(int triggerAtTicks) {
        this.triggerAtTicks = triggerAtTicks;
    }

    public SessionAction(int triggerAtTicks, String message, String sessionId) {
        this.triggerAtTicks = triggerAtTicks;
        this.sessionMessage = message;
        this.sessionId = sessionId;
    }

    public boolean tick(int currentTick, int sessionLength) {
        if (triggerAtTicks < 0) {
            int remaining = sessionLength-currentTick;
            if (hasTriggered && remaining > -triggerAtTicks) {
                hasTriggered = false;
            }
            if (hasTriggered) return true;
            if (remaining <= -triggerAtTicks) {
                hasTriggered = true;
                SessionTranscript.triggerSessionAction(sessionId);
                trigger();
                return true;
            }
            return false;
        }
        else {
            if (hasTriggered && triggerAtTicks > currentTick) {
                hasTriggered = false;
            }
            if (hasTriggered) return true;
            if (triggerAtTicks <= currentTick) {
                hasTriggered = true;
                SessionTranscript.triggerSessionAction(sessionId);
                trigger();
                return true;
            }
            return false;
        }
    }

    public int getTriggerTime() {
        return triggerAtTicks;
    }

    public abstract void trigger();
}
