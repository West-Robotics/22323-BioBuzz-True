package org.firstinspires.ftc.teamcode.IronNestBEE;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.Range;

/**
 * TeleOp OpMode designed for real-time tuning of the launcher subsystem's flywheel control parameters.
 * <p>
 * Uses {@link Config} to allow dynamic tuning of feedforward ({@link #kS}, {@link #kV}) and feedback ({@link #kP})
 * parameters as well as target RPM via FTC Dashboard. Also demonstrates main battery voltage compensation
 * to maintain consistent launcher velocity under varying battery charge levels.
 */
@TeleOp(name = "Tuner")
@Config
public class Tuner extends OpMode {

    /** Subsystem responsible for flywheel motor and angle positioning. */
    Launcher launcher;

    /** Tollbooth right gate servo mechanism. */
    private Tollbooth TbR;

    /** Tollbooth left gate servo mechanism. */
    private Tollbooth TbL;

    /** Intake motor mechanism. */
    private Intake In;

    /** Target rotational velocity of the launcher flywheel in RPM (FTC Dashboard configurable). */
    public static double goalRPM = 4775;

    /** Static friction feedforward coefficient kS (FTC Dashboard configurable). */
    public static double kS = 0.067;

    /** Velocity feedforward coefficient kV (FTC Dashboard configurable). */
    public static double kV = 0.00017;

    /** Proportional feedback gain kP for RPM error correction (FTC Dashboard configurable). */
    public static double kP = 0.002;

    /** Flag enabling or disabling main battery voltage compensation (FTC Dashboard configurable). */
    public static boolean voltageCompensationEnabled = true;

    /** Reference battery voltage used as baseline for voltage compensation (FTC Dashboard configurable). */
    public static double referenceBatteryVolts = 12.0;

    /** Sensor measuring the current battery voltage of the robot. */
    private VoltageSensor batteryVoltage;

    /**
     * Initializes hardware components, FTC Dashboard telemetry integration, and voltage sensor.
     */
    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        launcher = new Launcher(hardwareMap, telemetry);
        TbR = new Tollbooth(hardwareMap, false);
        TbL = new Tollbooth(hardwareMap, true);
        In = new Intake(hardwareMap);
        batteryVoltage = hardwareMap.voltageSensor.iterator().next();
    }

    /**
     * Main control loop executed repeatedly while the OpMode is running.
     * <p>
     * Calculates combined feedforward and feedback power required to reach target RPM,
     * applies battery voltage compensation if enabled, commands hardware, and posts telemetry
     * to both the Driver Station and FTC Dashboard.
     */
    @Override
    public void loop() {
        double error = goalRPM - launcher.getSpeed();

        double feedback = error * kP;

        double feedforward = (kS * Math.signum(goalRPM)) + (kV * goalRPM);

        double power = feedback + feedforward;

        double batteryVolts = batteryVoltage.getVoltage();

        boolean voltageValid =
                Double.isFinite(batteryVolts) && batteryVolts > 0.0;
        boolean compensationActive =
                voltageCompensationEnabled && voltageValid;

        double requestedVolts = power * referenceBatteryVolts;

        double requestedPower = power;

        if (compensationActive) {
            requestedPower = requestedVolts / batteryVolts;
        }

        double commandedPower = Range.clip(requestedPower, -1.0, 1.0);

        In.spin();
        launcher.pollen();
        launcher.spin(commandedPower);

        if (gamepad1.x) {
            TbR.open(false);
            TbL.open(true);
        } else {
            TbR.close(false);
            TbL.close(true);
        }

        telemetry.addData("Active kP", kP);
        telemetry.addData("Target RPM", goalRPM);
        telemetry.addData("Measured RPM", launcher.getSpeed());
        telemetry.addData("Feedback", feedback);
        telemetry.addData("Requested power", power);
        telemetry.addData("Commanded power", commandedPower);
        telemetry.update();
    }
}
