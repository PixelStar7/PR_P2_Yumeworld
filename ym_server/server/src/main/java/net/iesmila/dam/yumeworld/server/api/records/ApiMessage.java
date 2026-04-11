package net.iesmila.dam.yumeworld.server.api.records;

public record ApiMessage<T>(
        String action,
        T data
) {}