import lib.Matrix;
import lib.Quaternion;
import lib.Vector;
import processing.core.PApplet;
import processing.core.PVector;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class QuaternionLearn extends PApplet
{
    int windowWidth = 1280;
    int windowHeight = 700;

    Matrix coordinateSystem;
    public float[] angles = {0.0f,0.0f,0.0f,0.0f};

    Quaternion ori = new Quaternion(1.0f,0.f,0.0f,0.0f);

    float roll, pitch, head = 0.0f;
    float heading, attitude, bank = 0.0f;

    public void setup()
    {
        size(windowWidth, windowHeight, OPENGL);
        set(ori);

    }


    public void draw()
    {
        background(0xFF000000);

        text("xAngle: " + getFormatedFloat(ori.x), 100, 100);
        text("yAngle: " + getFormatedFloat(ori.y), 100, 140);
        text("zAngle: " + getFormatedFloat(ori.z), 100, 180);
        text("w: " + getFormatedFloat(ori.w), 100, 220);


        text("roll: " + getFormatedFloat(roll), 700, 100);
        text("pitch: " + getFormatedFloat(pitch), 700, 140);
        text("h: " + getFormatedFloat(head), 700, 180);

        text("roll: " + getFormatedFloat(heading), 900, 100);
        text("pitch: " + getFormatedFloat(attitude), 900, 140);
        text("h: " + getFormatedFloat(bank), 900, 180);


        angles = ori.getAxisAngle(ori);

        pushMatrix();
        translate(windowWidth / 2, windowHeight / 2, 0);

        rotate(angles[0], angles[1], angles[2], angles[3]);
        box(100, 12, 400);
        popMatrix();
    }



    public void mouseClicked()
    {

        float rotAngle = 90.0f * DEG_TO_RAD;

        if(mouseButton == LEFT)
        {
            Quaternion rot = new Quaternion((float)Math.cos(rotAngle/2), 0, (float)Math.sin(rotAngle/2), 0);
            rot.normalize();
            ori.multiply(ori, rot);
        }
        else
        {
            Quaternion rot = new Quaternion((float)Math.cos(rotAngle/2), 0, 0, (float)Math.sin(rotAngle/2));
            rot.normalize();
            ori.multiply(ori, rot);
        }


        ori.normalize();


        float gx = 2 * (ori.x * ori.z - ori.w * ori.y);
        float gy = 2 * (ori.w * ori.x - ori.y * ori.z);
        float gz = ori.w * ori.w - ori.x * ori.x - ori.y*ori.y + ori.z*ori.z;

        roll = (float)Math.atan(gy / Math.sqrt(gx*gx + gz*gz)) * 180/PI + 180;
        pitch = (float)Math.atan(gx / Math.sqrt(gy*gy + gz*gz)) * 180/PI + 180;
        head = (float)Math.atan2(2 * ori.x * ori.y - 2 * ori.w * ori.z, 2 * ori.w * ori.w + 2 * ori.x * ori.x - 1) * 180/PI + 180;

        set(ori);
    }


    public void set(Quaternion q1) {
        double test = q1.x*q1.y + q1.z*q1.w;
        if (test > 0.499) { // singularity at north pole
            heading = 2 * atan2(q1.x,q1.w);
            attitude = (float)Math.PI/2;
            bank = 0;
            return;
        }
        if (test < -0.499) { // singularity at south pole
            heading = -2 * atan2(q1.x,q1.w);
            attitude = - (float)Math.PI/2;
            bank = 0;
            return;
        }
        double sqx = q1.x*q1.x;
        double sqy = q1.y*q1.y;
        double sqz = q1.z*q1.z;
        heading = atan2((float)(2*q1.y*q1.w-2*q1.x*q1.z) , (float)(1 - 2*sqy - 2*sqz)) * RAD_TO_DEG + 180;
        attitude = asin((float)(2*test)) * RAD_TO_DEG + 180;
        bank = atan2(2*q1.x*q1.w-2*q1.y*q1.z , (float)(1 - 2*sqx - 2*sqz)) * RAD_TO_DEG + 180;
    }


    public String getFormatedFloat(float value)
    {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(2);
        return df.format(value);
    }


    static public void main(String args[]) {
        PApplet.main(new String[]{"QuaternionLearn"});
    }
}
