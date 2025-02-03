package io.serverbench.client.lib;

import io.serverbench.client.lib.obj.Command;

import java.util.List;
import java.util.function.Consumer;

public record EventHandler(Consumer<List<Command>> cmd, Runnable open, Runnable close) {
}
