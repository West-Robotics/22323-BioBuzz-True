package org.firstinspires.ftc.teamcode.IronNestBEE;
import static com.pedropathing.ivy.Scheduler.schedule;
import static com.pedropathing.ivy.groups.Groups.parallel;
import static com.pedropathing.ivy.groups.Groups.sequential;
import com.pedropathing.ivy.Command;
import com.pedropathing.ivy.Scheduler;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
@Disabled
@Autonomous(name= "matchSetup", group = "Autonomous")
public class matchSetup extends OpMode {
    private DriveTrain bee;
    private Launcher Ln;
    private Tollbooth TbL;
    private Tollbooth TbR;
    private Intake In;
    private Hood doomAndDespair;
    private FlowerRetriever FT;

    @Override
    public void init() {
        Scheduler.reset();
        // 2. Initialize them here using hardwareMap
        bee = new DriveTrain(hardwareMap);
        doomAndDespair = new Hood(hardwareMap);
        Ln = new Launcher(hardwareMap,telemetry);
        TbL = new Tollbooth(hardwareMap,true);
        TbR = new Tollbooth(hardwareMap,false);
        In = new Intake(hardwareMap);
        FT = new FlowerRetriever(hardwareMap);
    }


    @Override
    public void start(){
        final Command autonomous = sequential(
                parallel(
                        doomAndDespair.stowCommand(),
                        FT.upCommand(),
                        bee.Stop()
                )
        );
        schedule(autonomous);
    }

    @Override
    public void loop(){
        Scheduler.execute();
    }
}
