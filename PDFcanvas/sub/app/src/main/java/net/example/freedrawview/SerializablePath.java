package net.example.freedrawview;

import android.graphics.Path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Riccardo Moro on 11/4/2016.
 */

class SerializablePath extends Path implements Serializable {
    SerializablePath() {
        super();
    }

    SerializablePath(SerializablePath path) {
        super(path);
        for (PathAction act : path.actions)
            actions.add(act);
    }

    public ArrayList<PathAction> actions = new ArrayList<SerializablePath.PathAction>();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(actions);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        actions = (ArrayList)in.readObject();
        drawThisPath();
    }

    @Override
    public void moveTo(float x, float y) {
        actions.add(new ActionMove(x, y));
        super.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y){
        actions.add(new ActionLine(x, y));
        super.lineTo(x, y);
    }

    private void drawThisPath(){
        for(PathAction p : actions){
            if(p.getType().equals(PathAction.PathActionType.MOVE_TO)){
                super.moveTo(p.getX(), p.getY());
            } else if(p.getType().equals(PathAction.PathActionType.LINE_TO)){
                super.lineTo(p.getX(), p.getY());
            }
        }
    }

    public interface PathAction extends Serializable {
        public enum PathActionType {LINE_TO,MOVE_TO};
        public PathActionType getType();
        public float getX();
        public float getY();
    }

    public class ActionMove implements PathAction, Serializable{
        private float x,y;

        public ActionMove(float x, float y){
            this.x = x;
            this.y = y;
        }

        @Override
        public PathActionType getType() {
            return PathActionType.MOVE_TO;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

    }

    public class ActionLine implements PathAction, Serializable{
        private float x,y;

        public ActionLine(float x, float y){
            this.x = x;
            this.y = y;
        }

        @Override
        public PathActionType getType() {
            return PathActionType.LINE_TO;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

    }
}