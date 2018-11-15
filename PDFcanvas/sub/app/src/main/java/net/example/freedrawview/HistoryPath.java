package net.example.freedrawview;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by Riccardo Moro on 9/27/2016.
 */

public class HistoryPath implements Parcelable, Serializable {
    private SerializablePath path;
    private SerializablePaint paint;
    private float originX, originY;
    public float finalX, finalY;
    public boolean isCircle;
    private boolean isPoint;

    HistoryPath(@NonNull SerializablePath path, @NonNull SerializablePaint paint,
                       float originX, float originY,float finalX, float finalY, boolean isPoint, boolean isCircle) {
        this.path = path;
        this.paint = paint;
        this.originX = originX;
        this.originY = originY;
        this.finalX = finalX;
        this.finalY = finalY;
        this.isPoint = isPoint;
        this.isCircle = isCircle;
    }

    private HistoryPath(Parcel in) {
        originX = in.readFloat();
        originY = in.readFloat();
        isPoint = in.readByte() != 0;
    }

    public SerializablePath getPath() {
        return path;
    }

    public void setPath(SerializablePath path) {
        this.path = path;
    }

    public SerializablePaint getPaint() {
        return paint;
    }

    public void setPaint(SerializablePaint paint) {
        this.paint = paint;
    }

    public boolean isPoint() {
        return isPoint;
    }

    public void setPoint(boolean point) {
        isPoint = point;
    }

    public float getOriginX() {
        return originX;
    }

    public void setOriginX(float originX) {
        this.originX = originX;
    }

    public float getOriginY() {
        return originY;
    }

    public void setOriginY(float originY) {
        this.originY = originY;
    }


    // Parcelable stuff
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(path);
        dest.writeSerializable(paint);
        dest.writeFloat(originX);
        dest.writeFloat(originY);
        dest.writeByte((byte) (isPoint ? 1 : 0));
    }

    // Parcelable CREATOR class
    public static final Creator<HistoryPath> CREATOR = new Creator<HistoryPath>() {
        @Override
        public HistoryPath createFromParcel(Parcel in) {
            return new HistoryPath(in);
        }

        @Override
        public HistoryPath[] newArray(int size) {
            return new HistoryPath[size];
        }
    };
}
