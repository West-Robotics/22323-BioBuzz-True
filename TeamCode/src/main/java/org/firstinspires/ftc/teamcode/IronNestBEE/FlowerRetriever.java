package org.firstinspires.ftc.teamcode.IronNestBEE;

import com.pedropathing.ivy.Command;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class FlowerRetriever {
    private final Servo swivel;
    public FlowerRetriever(HardwareMap hardwareMap){
        swivel = hardwareMap.get(Servo.class, "hockeyL");
        this.up();
    }
    public void up(){
        swivel.setPosition(0.55);
    }
    public void down(){
        swivel.setPosition(0);
    }
    public Command upDown(Gamepad gamepad, Gamepad secondaryGamepad){
        return Command.build()
                .setExecute(()->{
                    if(gamepad.y || secondaryGamepad.y){
                        this.up();
                    } else if (gamepad.b || secondaryGamepad.b) {
                        this.down();
                    }

                }).requiring(this);
    }
    public Command upCommand() {
        return Command.build()
                .setExecute(this::up)
                .requiring(this);
    }
}
