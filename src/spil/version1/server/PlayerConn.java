package spil.version1.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

public record PlayerConn(Socket socket, DataOutputStream outToClient, ObjectOutputStream objectToClient, BufferedReader stringFromClient, String name) implements Comparable<PlayerConn> {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerConn that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(PlayerConn o) {
        return name.compareTo(o.name);
    }
}
