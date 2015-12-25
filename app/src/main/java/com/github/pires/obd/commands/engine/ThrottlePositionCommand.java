package com.github.pires.obd.commands.engine;

import com.github.pires.obd.commands.PercentageObdCommand;
import com.github.pires.obd.enums.AvailableCommandNames;

/**
 * Read the throttle position in percentage.
 */
public class ThrottlePositionCommand extends PercentageObdCommand {

    private float throttle;
    /**
     * Default ctor.
     */
    public ThrottlePositionCommand() {
        super("01 11");
    }

    /**
     * Copy ctor.
     *
     * @param other a {@link ThrottlePositionCommand} object.
     */
    public ThrottlePositionCommand(ThrottlePositionCommand other) {
        super(other);
    }

    @Override
    protected void performCalculations() {
        // Ignore first two bytes [hh hh] of the response.
        throttle = (buffer.get(2))*100/255;
    }

    @Override
    public String getFormattedResult() {
        return String.format("%.2f%s", throttle, getResultUnit());
    }

    @Override
    public String getResultUnit() {
        return "%";
    }

    /**
     *
     */
    @Override
    public String getName() {
        return AvailableCommandNames.THROTTLE_POS.getValue();
    }

}
