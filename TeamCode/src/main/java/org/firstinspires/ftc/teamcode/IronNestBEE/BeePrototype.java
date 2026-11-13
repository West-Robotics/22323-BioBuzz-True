package org.firstinspires.ftc.teamcode.IronNestBEE;
import static com.pedropathing.ivy.Scheduler.schedule;
import com.pedropathing.ivy.Scheduler;
import com.pedropathing.ivy.groups.Groups;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
@Disabled
@TeleOp(name = "Betotype", group = "TeleOp")
public class BeePrototype extends OpMode {
    @Override
    public void init() {
        Scheduler.reset();
        Launcher beeLaunch = new Launcher(hardwareMap,telemetry);
        // Schedule the default drive command to run in a loop
        schedule(
                Groups.loop(
                       beeLaunch.Launch(gamepad1)
                ));
    }

    @Override
    public void loop() {
        Scheduler.execute();
    }
}
