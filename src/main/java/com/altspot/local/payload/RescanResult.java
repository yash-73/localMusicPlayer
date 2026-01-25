package com.altspot.local.payload;

public class RescanResult {

    public int inserted;
    public int deleted;
    public int updated;

    public RescanResult(int inserted, int deleted, int updated) {
        this.inserted = inserted;
        this.deleted = deleted;
        this.updated = updated;
    }
}
