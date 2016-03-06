package com.moorango.medli.lib.eventbus.events;

/**
 * Created by cmac147 on 3/4/16.
 */
public class FinishOnboardingEvent extends Event {

    private String message;
    public int status;

    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    public FinishOnboardingEvent(String message, int status) {
        this.status = status;
        this.message = message;
    }

    public String getMessage() {
        if (message != null)
            return this.message;
        else return "FAILURE";
    }

    public boolean success() {
        return (status == SUCCESS);
    }
}
