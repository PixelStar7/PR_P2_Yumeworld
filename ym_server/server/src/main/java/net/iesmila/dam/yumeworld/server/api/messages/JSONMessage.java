package net.iesmila.dam.yumeworld.server.api.records;

public record JSONMessage<T>(
        String action,
        String messageType,
        T data
) {}