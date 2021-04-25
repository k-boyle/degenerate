package kboyle.degenerate.commands.modules;

import kboyle.degenerate.commands.DegenerateContext;
import kboyle.degenerate.commands.DegenerateModule;
import kboyle.degenerate.commands.preconditions.RequireBotOwner;
import kboyle.oktane.reactive.ReactiveCommandHandler;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.module.ReactiveModule;
import kboyle.oktane.reactive.module.annotations.Aliases;
import kboyle.oktane.reactive.module.annotations.Name;
import kboyle.oktane.reactive.module.annotations.Remainder;
import kboyle.oktane.reactive.module.annotations.Require;
import kboyle.oktane.reactive.processor.OktaneModule;
import kboyle.oktane.reactive.results.command.CommandResult;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static kboyle.degenerate.Constants.DEGENERATE_COLOUR;
import static kboyle.degenerate.Markdown.CODE;

@Name("Misc")
@OktaneModule
public class MiscModule extends DegenerateModule {
    private final ReactiveCommandHandler<DegenerateContext> commandHandler;

    public MiscModule(ReactiveCommandHandler<DegenerateContext> commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Aliases("ping")
    public CommandResult ping() {
        return reply("pong");
    }

    @Aliases("owner")
    @Require(precondition = RequireBotOwner.class)
    public CommandResult owner() {
        return reply("ur cute");
    }

    @Aliases("help")
    public Mono<CommandResult> help() {
        return context().client.getSelf()
            .map(degenerate ->
                createMessage(messageSpec ->
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
    public Mono<CommandResult> help(@Remainder ReactiveModule module) {
        return context().client.getSelf()
            .map(degenerate ->
                createMessage(messageSpec ->
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
    public CommandResult help(@Remainder List<ReactiveCommand> commands) {
        return reply("not done yet lol");
    }

    private static String helpFormat(Stream<String> strings) {
        return strings.map(CODE::format)
            .collect(Collectors.joining(", "));
    }

    @Aliases("purge")
    public Mono<CommandResult> purge(int count) {
        return context().channel.bulkDeleteMessages(context().channel.getMessagesBefore(context().message.getId()).take(count))
            .then(embed("Deleted %d messages", count).mono());
    }
}
