package com.sample.usbdemo.event;

import java.io.File;

public class DownloadEvent {
    private File file;

    public DownloadEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
