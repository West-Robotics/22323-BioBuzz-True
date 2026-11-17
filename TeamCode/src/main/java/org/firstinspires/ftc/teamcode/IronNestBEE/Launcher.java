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

    /** Static friction feedforward coefficient candidate for speed control. */
    public static double kS = 0.1;

    /** Voltage sensor used for main battery voltage compensation. */
    private VoltageSensor batteryVoltage;

    /** Encoder counts per motor shaft revolution (28 ticks per revolution). */
    final double encoderCPM = 28;

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

    /**
     * Spins the flywheel using battery voltage compensation based on nominal baseline reference voltage (12.5V).
     * <p>
     * Calculates required motor power to maintain constant output voltage relative to battery state,
     * falling back to fixed reference power if battery voltage reading is invalid.
     * <p>
     * TODO:
     * Find out the most consistent battery level and
     * power applied and change referencePower and
     * reference battery volts to match.
     * Also, get the following information:
     * The motor model, gearbox ratio if any, any external gearing;
     * The battery Voltage of successful shots;
     * The velocity of the flywheel at those successful shots.
     */
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
    public void spinWithPID() {

    }

    /**
     * Stops the flywheel motor by setting power to 0.
     */
    public void stop() {
        Flywheel.setPower(0);
    }

    /**
     * Sets the launch adjustment servo to the pollen scoring position preset (0.95).
     */
    public void pollen() {
        LaunchAdjust.setPosition(0.95);
    }

    /**
     * Creates a PedroPathing Ivy {@link Command} for driver-controlled launcher operations.
     *
     * @param gamepad Gamepad instance supplying driver input (triggers and dpad).
     * @return Command executing launch and position adjustment based on gamepad inputs.
     */
    public Command Launch(Gamepad gamepad) {
        return Command.build()
                .setExecute(() -> {
                    if (gamepad.right_trigger > .2 || gamepad.x) {
                        this.spin();
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
    public Command autoLaunch() {
        return Command.build()
                .setExecute(() -> {
                    this.spin();
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
