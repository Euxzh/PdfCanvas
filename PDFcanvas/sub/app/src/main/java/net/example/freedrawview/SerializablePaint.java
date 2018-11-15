package net.example.freedrawview;

import android.graphics.Paint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Riccardo Moro on 11/4/2016.
 */

class SerializablePaint extends Paint implements Serializable {
    SerializablePaint(int flags) {
        super(flags);
    }

    SerializablePaint(SerializablePaint paint) {
        super(paint);
    }

    /** Included for serialization - write this layer to the output stream. */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(getStyle());
        out.writeInt(getColor());
        out.writeFloat(getStrokeWidth());
    }

    /** Included for serialization - read this object from the supplied input stream. */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        setStyle((Style) in.readObject());
        setColor(in.readInt());
        setStrokeWidth(in.readFloat());
    }
}
