package kboyle.degenerate.commands;

import kboyle.degenerate.Constants;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.module.DiscordModuleBase;

public abstract class DegenerateModule extends DiscordModuleBase<DiscordCommandContext> {
    protected CommandResult embed(String content) {
        return embed(spec -> spec.setDescription(content).setColor(Constants.DEGENERATE_COLOUR));
    }

    protected CommandResult embed(String content, Object... args) {
        return embed(String.format(content, args));
    }
}
