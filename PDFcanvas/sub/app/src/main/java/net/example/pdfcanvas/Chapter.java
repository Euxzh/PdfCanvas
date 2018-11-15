package net.example.pdfcanvas;

import java.io.Serializable;

public class Chapter implements Serializable {
    public String showenText;
    public String fileName;

    public Chapter() {
        this("", "");
    }

    public Chapter(String txt, String pth) {
        this.showenText = txt;
        this.fileName = pth;
    }
}
