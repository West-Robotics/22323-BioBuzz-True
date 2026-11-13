package org.firstinspires.ftc.teamcode.IronNestBEE;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
@TeleOp(name = "Tuner")
@Config
public class Tuner extends OpMode {
    Launcher launcher;
    public static double kS = 0.1;
    @Override
    public void init() {
        launcher = new Launcher(hardwareMap, telemetry);
    }

    @Override
    public void loop() {
        launcher.spin(kS);
        telemetry.addData("Ks",kS);
        telemetry.update();

    }
}
