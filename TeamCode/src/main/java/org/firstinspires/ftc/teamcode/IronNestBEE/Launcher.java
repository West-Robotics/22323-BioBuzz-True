package org.firstinspires.ftc.teamcode.IronNestBEE;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.ivy.Command;
import com.pedropathing.utils.Timer;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Subsystem controlling the robot's flywheel launcher motor and launch angle adjust servo.
 * <p>
 * Provides methods for voltage-compensated flywheel spin control, explicit power control,
 * servo position presets, and PedroPathing Ivy commands for teleop and autonomous routines.
 */
@Config
public class Launcher {

    /** Telemetry instance for outputting status and debug information. */
    private final Telemetry telemetry;

    /** Flywheel DC motor driven in velocity or open-loop power modes. */
    private final DcMotorEx Flywheel;

    /** Servo controlling the launch trajectory angle/position. */
    private final Servo LaunchAdjust;

    /** Timer instance for timing-dependent launcher operations. */
    private Timer timer;

    /** Encoder counts per motor shaft revolution (28 ticks per revolution). */
    final double encoderCPM = 28;

    /** Static friction feedforward coefficient kS (FTC Dashboard configurable). */
    public static double kS = 0.067;

    /** Velocity feedforward coefficient kV (FTC Dashboard configurable). */
    public static double kV = 0.00017;

    /** Proportional feedback gain kP for RPM error correction (FTC Dashboard configurable). */
    public static double kP = 0.0016;

    /** Flag enabling or disabling main battery voltage compensation (FTC Dashboard configurable). */
    public static boolean voltageCompensationEnabled = true;

    /** Reference battery voltage used as baseline for voltage compensation (FTC Dashboard configurable). */
    public static double referenceBatteryVolts = 12.0;

    /** Sensor measuring the current battery voltage of the robot. */
    private VoltageSensor batteryVoltage;

    /**
     * Initializes all hardware components related to launching.
     *
     * @param hardwareMap The OpMode's hardware map.
     * @param telemetry   The OpMode's built-in or combined telemetry.
     */
    public Launcher(HardwareMap hardwareMap, Telemetry telemetry) {
        Flywheel = hardwareMap.get(DcMotorEx.class, "launcher");
        LaunchAdjust = hardwareMap.get(Servo.class, "launchAdjust");
        Flywheel.setDirection(DcMotorSimple.Direction.FORWARD);
        Flywheel.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        batteryVoltage = hardwareMap.voltageSensor.iterator().next();
        this.telemetry = telemetry;
    }


    public void spin() {
        double power = 0.87;
        double referencePower = 0.87;
        double referenceBatteryVolts = 12.5;
        double batteryVolts = batteryVoltage.getVoltage();
        boolean voltageValid = Double.isFinite(batteryVolts) && batteryVolts > 0.0;

        if (!Double.isFinite(batteryVolts) || batteryVolts <= 0) {
            Flywheel.setPower(power);
        } else {
            power = (referencePower * referenceBatteryVolts) / batteryVolts;
            Flywheel.setPower(Range.clip(power, -1.0, 1.0));
        }

        telemetry.addData("RPM", this.getSpeed());
        telemetry.addData("Flywheel ticks/sec speed, ", Flywheel.getVelocity());
        telemetry.addData("Baseline power under 12V, ", referencePower);
        telemetry.addData("Measured battery level (V), ", batteryVolts);
        telemetry.addData("adjusted power, ", power);
        telemetry.addData("Control mode", voltageValid ? "Voltage compensated" : "Fixed-power fallback");
        telemetry.addData("final power requested, ", Range.clip(power, -1.0, 1.0));
        telemetry.update();
    }

    /**
     * Spins the flywheel at a specific commanded power level.
     *
     * @param power Commanded power level ranging from -1.0 to 1.0.
     */
    public void spin(double power) {
        Flywheel.setPower(power);
        telemetry.addData("RPM", this.getSpeed());
        telemetry.addData("TicksPerSeconds", Flywheel.getVelocity());
    }

    /**
     * Placeholder method for implementing closed-loop PID velocity control.
     */
    public void spinWithPID(double goalRPM) {
        double error = goalRPM - this.getSpeed();

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

        this.spin(commandedPower);
        telemetry.addData("Active kP", kP);
        telemetry.addData("Target RPM", goalRPM);
        telemetry.addData("Measured RPM", this.getSpeed());
        telemetry.addData("Feedback", feedback);
        telemetry.addData("Requested power", power);
        telemetry.addData("Commanded power", commandedPower);
        telemetry.update();
    }

    public void stop() {
        Flywheel.setPower(0);
    }

    public void pollen() {
        LaunchAdjust.setPosition(0.95);
    }

    /**
     * Creates a PedroPathing Ivy {@link Command} for driver-controlled launcher operations.
     *
     * @param gamepad Gamepad instance supplying driver input (triggers and dpad).
     * @return Command executing launch and position adjustment based on gamepad inputs.
     */
    public Command Launch(Gamepad gamepad, Gamepad secondaryGamepad, double rpm) {
        return Command.build()
                .setExecute(() -> {
                    if (gamepad.a || gamepad.x || gamepad.right_trigger>0.2 || secondaryGamepad.right_trigger>0.2) {
                        this.spinWithPID(rpm);
                    } else {
                        this.stop();
                    }
                    if (gamepad.dpad_down) {
                        // pollen position
                        LaunchAdjust.setPosition(0.95);
                    }
                    if (gamepad.dpad_up) {
                        // nectar position
                        LaunchAdjust.setPosition(0.49);
                    }
                    if (gamepad.dpad_right) {
                        // receiving position
                        LaunchAdjust.setPosition(0.22);
                    }
                }).requiring(this);
    }

    /**
     * Creates a PedroPathing Ivy {@link Command} for autonomous launching.
     *
     * @return Command that spins up the flywheel and sets launch angle to pollen position.
     */
    public Command autoLaunch(double rpm) {
        return Command.build()
                .setExecute(() -> {
                    this.spinWithPID(rpm);
                    this.pollen();
                })
                .setEnd(endCondition -> this.stop())
                .requiring(this);
    }

    /**
     * Computes the current flywheel speed in Revolutions Per Minute (RPM).
     *
     * @return Flywheel speed in RPM.
     */
    public double getSpeed() {
        return (Flywheel.getVelocity() / encoderCPM) * 60;
    }

    /**
     * Sets target flywheel velocity directly in encoder ticks per second.
     *
     * @param speed Target motor velocity in ticks/second.
     */
    public void setSpeed(double speed) {
        Flywheel.setVelocity(speed);
    }
}
