package kboyle.degenerate;

import kboyle.oktane.reactive.CommandContext;
import kboyle.oktane.reactive.ReactiveCommandHandler;
import kboyle.oktane.reactive.module.ReactiveModule;

import java.util.function.Consumer;
import java.util.stream.Stream;

public enum Utils {
    ;

    public static boolean insensitiveContains(String a, String b) {
        for (var i = a.length() - b.length(); i >= 0; i--)
            if (a.regionMatches(true, i, b, 0, b.length()))
                return true;
        return false;
    }

    // todo add to okane
    public static <T extends CommandContext> Stream<ReactiveModule> getAllModules(ReactiveCommandHandler<T> handler) {
        return handler.modules().stream().mapMulti(Utils::mapModule);
    }

    private static void mapModule(ReactiveModule module, Consumer<ReactiveModule> downStream) {
        downStream.accept(module);
        for (var child : module.children) {
            mapModule(child, downStream);
        }
    }
}
