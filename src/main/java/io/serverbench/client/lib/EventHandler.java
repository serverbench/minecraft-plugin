package io.serverbench.client.lib;

import io.serverbench.client.lib.obj.Command;
import io.serverbench.client.lib.obj.vote.VoteDisplay;

import java.util.List;
import java.util.function.Consumer;

public record EventHandler(
        Consumer<List<Command>> cmd,
        Consumer<List<VoteDisplay>> voteDisplay,
        Runnable open,
        Runnable close
) { }
