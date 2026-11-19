package org.firstinspires.ftc.teamcode.IronNestBEE;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.ServoImplEx;

public class Hood {
    private final ServoImplEx hood;
    private double hoodState;
    public Hood(HardwareMap hardwareMap) {
        hood = hardwareMap.get(ServoImplEx.class, "hood");
        PwmControl.PwmRange range = new PwmControl.PwmRange(500, 2500);
        hood.setPwmEnable();
        hood.setPwmRange(range);
        this.stowed();
        hoodState = 0;
    }
    public void stowed() { hood.setPosition(0.95); }
    public void launchOffset() {
        hood.setPosition(0.87);
    }
    public void deployed() {
        hood.setPosition(0.35);
    }

    public Command block(Gamepad gamepad, Gamepad secondaryGamepad) {
        return Command.build()
                .setExecute(() -> {
                    if ((gamepad.x || gamepad.a || secondaryGamepad.right_trigger > 0.2) && hoodState != 2){
                        this.launchOffset();
                        hoodState = 1;
                    } else if (gamepad.left_bumper || secondaryGamepad.a || hoodState == 1){
                        this.stowed();
                        hoodState = 0;
                    } else if (gamepad.right_bumper || secondaryGamepad.x) {
                        this.deployed();
                        hoodState = 2;
                    }
                }).requiring(this);

    }
    public Command launchOffsetCommand() {
        return Command.build()
                .setExecute(this::launchOffset)
                .requiring(this);
    }
    public Command stowCommand() {
        return Command.build()
                .setExecute(this::stowed)
                .requiring(this);
    }
}
