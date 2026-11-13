package org.firstinspires.ftc.teamcode.IronNestBEE;

import static com.qualcomm.robotcore.util.Range.clip;
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
import com.acmerobotics.dashboard.config.Config;


import org.firstinspires.ftc.robotcore.external.Telemetry;

@Config
public class Launcher {
    private final Telemetry telemetry;
    private final DcMotorEx Flywheel;
    private final Servo LaunchAdjust;
    private Timer timer;

    private VoltageSensor batteryVoltage;
    final double encoderCPM = 28;

    /**
     * Initialize all things related to launching here
     *
     * @param hardwareMap The OpMode's hardware map.
     * @param telemetry  The OpMode's built in telemetry.
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
     * TODO:
     * Find out the most consistent battery level and
     * power applied and change referencePower and
     * reference battery volts to match
     * Also, get the following information:
     * The motor model, gearbox ratio if any,
     * any external gearing;
     * The battery Voltage of successful shots
     * The velocity of the flywheel at those successful shots
     */
    public void spin() {
        double power = 0.87 ;
        double referencePower = 0.87;
        double referenceBatteryVolts = 12.5;
        double batteryVolts = batteryVoltage.getVoltage();
        boolean voltageValid =
                Double.isFinite(batteryVolts)
                        && batteryVolts > 0.0;
        if (!Double.isFinite(batteryVolts) || batteryVolts <= 0) {
            Flywheel.setPower(power);
        }else {
             power = (referencePower * referenceBatteryVolts) / batteryVolts;
            Flywheel.setPower(Range.clip(power,-1.0, 1.0));
        }
        telemetry.addData("Flywheel ticks/sec speed, ", Flywheel.getVelocity());
        telemetry.addData("Baseline power under 12V, ", referencePower);
        telemetry.addData("Measured battery level (V), ", batteryVolts);
        telemetry.addData("adjusted power, ", power);
        telemetry.addData("Control mode",
                voltageValid ? "Voltage compensated"
                        : "Fixed-power fallback");

        telemetry.addData("final power requested, " ,
                Range.clip(power, -1.0, 1.0));
        telemetry.update();
    }
    public  void spin(double power){
        Flywheel.setPower(power);
        telemetry.addData("RPM", this.getSpeed());
        telemetry.addData("TicksPerSeconds",Flywheel.getVelocity());
    }

    public void spinWithPID() {

    }
    public void stop(){
        Flywheel.setPower(0);
    }

    public void pollen(){
        LaunchAdjust.setPosition(0.95);
    }
    public Command Launch(Gamepad gamepad){
        return Command.build()
                .setExecute(()->{
                    if(gamepad.right_trigger>.2 || gamepad.x) {
                        this.spin();
                    }else{
                        this.stop();
                    }
                    if(gamepad.dpad_down){
                        //pollen position
                        LaunchAdjust.setPosition(0.95);
                    }
                    if(gamepad.dpad_up){
                        //nectar position
                        LaunchAdjust.setPosition(0.49);
                    }
                    if(gamepad.dpad_right){
                        //receiving position
                        LaunchAdjust.setPosition(0.22);
                    }
                        }).requiring(this);
    }
    public Command autoLaunch(){
        return Command.build()
                .setExecute(()->
                {this.spin();
                this.pollen();})
                .setEnd(endCondition -> this.stop())
                .requiring(this);
    }
    public double getSpeed(){
       return (Flywheel.getVelocity()/encoderCPM)*60;
    }
    //Experimental, test with getSpeed to see if it works
    public void setSpeed(double speed){
        Flywheel.setVelocity(speed);
    }


}
