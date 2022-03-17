package com.example.plug_assignment;

public class Message {
    String type="",sdp="",candidate="",id="";
    int label;

    public Message(){}

    public Message(String type, String sdp, String candidate, String id, int label) {
        this.type = type;
        this.sdp = sdp;
        this.candidate = candidate;
        this.id = id;
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSdp() {
        return sdp;
    }

    public void setSdp(String sdp) {
        this.sdp = sdp;
    }

    public String getCandidate() {
        return candidate;
    }

    public void setCandidate(String candidate) {
        this.candidate = candidate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", sdp='" + sdp + '\'' +
                ", candidate='" + candidate + '\'' +
                ", id='" + id + '\'' +
                ", label=" + label +
                '}';
    }
}
