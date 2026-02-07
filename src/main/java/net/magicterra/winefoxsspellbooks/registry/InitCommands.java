package net.magicterra.winefoxsspellbooks.registry;

import net.magicterra.winefoxsspellbooks.command.WsbCommand;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 注册指令
 *
 * @author Gardel &lt;gardel741@outlook.com&gt;
 * @since 2026-01-23 01:03
 */
public class InitCommands {
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        WsbCommand.register(event.getDispatcher());
    }
}
