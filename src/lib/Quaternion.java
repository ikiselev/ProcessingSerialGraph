package lib;

import processing.core.PVector;
//Кватернионы (используются для поворота векторв в пространстве) Взято из http://xgm.ru/forum/showthread.php?p=186798
public class Quaternion
{
    public float x, y, z, w; //Используются не 3, а 4 значения для поворота векторов в пространстве. То есть это не 3D а 4D вектор(грубо говоря)
    //Конструктор нулевого кватерниона
    public Quaternion()
    {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }

    public Quaternion(float w, float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float[] getAxisAngle(Quaternion q1) {
        float res[] = new float[4];

        if (q1.w > 1) q1.normalize(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
        res[0] = 2 * (float)Math.acos(q1.w); //angle
        float s = (float)Math.sqrt(1-q1.w*q1.w); // assuming quaternion normalised then w is less than 1, so term always positive.
        if (s < 0.001) { // test to avoid divide by zero, s is always positive due to sqrt
            // if s close to zero then direction of axis not important
            res[1] = q1.x; // if it is important that axis is normalised then replace with x=1; y=z=0;
            res[2] = q1.y;
            res[3] = q1.z;
        } else {
            res[1] = q1.x / s; // normalise axis
            res[2] = q1.y / s;
            res[3] = q1.z / s;
        }

        return res;
    }

    public Quaternion mult (Quaternion q) {
        float w = this.w*q.w - (this.x*q.x + this.y*q.y + this.z*q.z);

        float x = this.w*q.x + q.w*this.x + this.y*q.z - this.z*q.y;
        float y = this.w*q.y + q.w*this.y + this.z*q.x - this.x*q.z;
        float z = this.w*q.z + q.w*this.z + this.x*q.y - this.y*q.x;

        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }


    public Quaternion normalize() {
        float norme = (float)Math.sqrt(this.w*this.w + this.x*this.x + this.y*this.y + this.z*this.z);
        if (norme == 0.0)
        {
            w = 1.0f;
            x = y = z = 0.0f;
        }
        else
        {
            float recip = 1.0f/norme;

            w *= recip;
            x *= recip;
            y *= recip;
            z *= recip;
        }
        return this;
    }

    //Вычисление кватерниона для поворота вектора вокруг оси OX
    public Quaternion quatX(float x)
    {
        this.x = (float)Math.sin(x/2);
        this.y = 0;
        this.z = 0;
        this.w = (float)Math.cos(x/2);
        return this;
    }
    //Вычисление кватерниона для поворота вектора вокруг оси OY
    public Quaternion quatY(float y)
    {
        this.x = 0;
        this.y = (float)Math.sin(y/2);
        this.z = 0;
        this.w = (float)Math.cos(y/2);
        return this;
    }
    //Вычисление кватерниона для поворота вектора вокруг оси OZ
    public Quaternion quatZ(float z)
    {
        this.x = 0;
        this.y = 0;
        this.z = (float)Math.sin(z/2);
        this.w = (float)Math.cos(z/2);
        return this;
    }

    public Quaternion conjugate () {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    //Перемножение кватернионов для того чтобы повернуть по 2м или 3м осям одновременно.
    public Quaternion multiply(Quaternion q1 ,Quaternion q2)
    {
        float A=(q1.w+q1.x)*(q2.w+q2.x);
        float B=(q1.z-q1.y)*(q2.y-q2.z);
        float C=(q1.x-q1.w)*(q2.y+q2.z);
        float D=(q1.y+q1.z)*(q2.x-q2.w);
        float E=(q1.x+q1.z)*(q2.x+q2.y);
        float F=(q1.x-q1.z)*(q2.x-q2.y);
        float G=(q1.w+q1.y)*(q2.w-q2.z);
        float H=(q1.w-q1.y)*(q2.w+q2.z);

        this.w= B + (-E - F + G + H) * 0.5f;
        this.x= A - ( E + F + G + H) * 0.5f;
        this.y=-C + ( E - F + G - H) * 0.5f;
        this.z=-D + ( E - F - G + H) * 0.5f;
        return this;
    }
    //Вывод на экран
    public void print (String name)
    {
        System.out.printf("\n%s = \n %8.4f %8.4f %8.4f %8.4f\n", name, x, y, z, w);
    }

}
