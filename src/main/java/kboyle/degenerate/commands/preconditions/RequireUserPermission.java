package kboyle.degenerate.commands.preconditions;

import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.reactive.module.ReactiveCommand;
import kboyle.oktane.reactive.results.precondition.PreconditionResult;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class RequireUserPermission extends DegeneratePrecondition {
    private final PermissionSet permissions;

    public RequireUserPermission(String[] permissions) {
        var rawValue = Arrays.stream(permissions)
            .map(Permission::valueOf)
            .mapToLong(Permission::getValue)
            .reduce(0, (left, right) -> left | right);
        this.permissions = PermissionSet.of(rawValue);
    }

    @Override
    protected Mono<PreconditionResult> run(DegenerateContext context, ReactiveCommand command) {
        return context.author()
            .flatMap(author ->
                author.getBasePermissions()
                    .map(userPerms -> userPerms.and(permissions).equals(permissions))
                    .map(hasPermission -> {
                        if (hasPermission) {
                            return success();
                        }

                        return failure("%s is required to execute this command", permissions);
                    })
            );
    }
}
