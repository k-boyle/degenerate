package kboyle.degenerate.commands.preconditions;

import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import kboyle.degenerate.commands.DegenerateContext;
import kboyle.oktane.core.module.Command;
import kboyle.oktane.core.results.precondition.PreconditionResult;
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
    protected Mono<PreconditionResult> run(DegenerateContext context, Command command) {
        return context.author()
            .flatMap(Member::getBasePermissions)
            .map(userPerms -> {
                if (userPerms.and(permissions).equals(permissions)) {
                    return success();
                }

                return failure("%s is required to execute this command", permissions);
            });
    }
}
