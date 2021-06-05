package kboyle.degenerate.commands.modules;

import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.Permission;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.module.CommandModule;
import kboyle.oktane.core.module.annotations.Aliases;
import kboyle.oktane.core.module.annotations.Name;
import kboyle.oktane.core.module.annotations.Remainder;
import kboyle.oktane.core.results.command.CommandResult;
import kboyle.oktane.discord4j.DiscordCommandContext;
import kboyle.oktane.discord4j.DiscordCommandHandler;
import kboyle.oktane.discord4j.precondition.PermissionTarget;
import kboyle.oktane.discord4j.precondition.RequireBotOwner;
import kboyle.oktane.discord4j.precondition.RequirePermission;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kboyle.degenerate.Constants.DEGENERATE_COLOUR;
import static kboyle.degenerate.Markdown.CODE;

@Name("Misc")
public class MiscModule extends DegenerateModule {
    private final DiscordCommandHandler<DiscordCommandContext> commandHandler;

    public MiscModule(DiscordCommandHandler<DiscordCommandContext> commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Aliases("ping")
    public CommandResult ping() {
        return reply("pong");
    }

    @Aliases("owner")
    @RequireBotOwner
    public CommandResult owner() {
        return reply("ur cute");
    }

    @Aliases("help")
    public Mono<CommandResult> help() {
        return context().client().getSelf()
            .map(degenerate ->
                reply(messageSpec ->
                    messageSpec.setEmbed(embedSpec -> {
                        var moduleNames = commandHandler.modules().stream()
                            .map(module -> module.name)
                            .map(CODE::format)
                            .collect(Collectors.joining(", "));

                        embedSpec.setColor(DEGENERATE_COLOUR)
                            .setTitle("There is no helping a degenerate")
                            .setThumbnail(degenerate.getAvatarUrl())
                            .setFooter("Execute \"@degenerate module\" to view help for a specific module", null)
                            .addField("Modules", moduleNames, false);
                    })
                )
            );
    }

    @Aliases("module")
    public Mono<CommandResult> help(@Remainder CommandModule module) {
        return context().client().getSelf()
            .map(degenerate ->
                reply(messageSpec ->
                    messageSpec.setEmbed(embedSpec -> {
                        var description = module.description.orElse("Ping the dev to add a description >:[");

                        embedSpec.setColor(DEGENERATE_COLOUR)
                            .setTitle("There is no helping a degenerate")
                            .setDescription(description)
                            .setThumbnail(degenerate.getAvatarUrl())
                            .setFooter("Execute \"@degenerate command\" to view help for a specific command", null);

                        module.parent.ifPresent(parent -> embedSpec.addField("Parent Module", CODE.format(parent.name), false));

                        if (!module.groups.isEmpty()) {
                            var groupsStr = helpFormat(module.groups.stream());
                            embedSpec.addField("Groups", groupsStr, false);
                        }

                        var groupCommands = module.commands.stream()
                            .<String>mapMulti((command, downstream) -> {
                                if (command.aliases.stream().anyMatch(String::isEmpty)) {
                                    downstream.accept(command.name);
                                }
                            })
                            .distinct();

                        var groupCommandsStr = helpFormat(groupCommands);

                        if (!groupCommandsStr.isEmpty()) {
                            embedSpec.addField("Group Commands", groupCommandsStr, false);
                        }

                        if (!module.commands.isEmpty()) {
                            var commands = module.commands.stream()
                                .map(command -> command.name)
                                .distinct();

                            var commandAliases = module.commands.stream()
                                .<String>mapMulti((command, downstream) ->
                                    command.aliases.stream().filter(alias -> !alias.isEmpty()).forEach(downstream)
                                )
                                .distinct();

                            embedSpec.addField("Commands", helpFormat(commands), false)
                                .addField("Command Aliases", helpFormat(commandAliases), false);
                        }

                        if (!module.children.isEmpty()) {
                            var subModules = module.children.stream()
                                .map(child -> child.name)
                                .distinct();

                            embedSpec.addField("Sub Modules", helpFormat(subModules), false);
                        }
                    })
                )
            );
    }

    @Aliases("command")
    public CommandResult help(@Remainder List<Command> commands) {
        return reply("not done yet lol");
    }

    private static String helpFormat(Stream<String> strings) {
        return strings.map(CODE::format)
            .collect(Collectors.joining(", "));
    }

    @Aliases("purge")
    @RequireBotOwner(group = "owner or manage messages")
    @RequirePermission(target = PermissionTarget.USER, permissions = Permission.MANAGE_MESSAGES, group = "owner or manage messages")
    @RequirePermission(target = PermissionTarget.BOT, permissions = Permission.MANAGE_MESSAGES)
    public Mono<CommandResult> purge(int count) {
        return context().channel()
            .ofType(TextChannel.class)
            .flatMapMany(channel -> channel.bulkDeleteMessages(channel.getMessagesBefore(context().message().getId()).take(count)))
            .then(embed("Deleted %d messages", count).mono());
    }
}
