/*
 * FastBack - Fast, incremental Minecraft backups powered by Git.
 * Copyright (C) 2022 pcal.net
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */

package net.pcal.fastback.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.pcal.fastback.logging.UserLogger;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.pcal.fastback.commands.Commands.SUCCESS;
import static net.pcal.fastback.commands.Commands.gitOp;
import static net.pcal.fastback.commands.Commands.subcommandPermission;
import static net.pcal.fastback.utils.Executor.ExecutionLock.NONE;

enum RestoreCommand implements Command {

    INSTANCE;

    private static final String COMMAND_NAME = "restore";
    private static final String ARGUMENT = "snapshot";

    @Override
    public void register(LiteralArgumentBuilder<ServerCommandSource> argb, PermissionsFactory<ServerCommandSource> pf) {
        argb.then(
                literal(COMMAND_NAME).
                        requires(subcommandPermission(COMMAND_NAME, pf)).then(
                                argument(ARGUMENT, StringArgumentType.string()).
                                        suggests(SnapshotNameSuggestions.local()).
                                        executes(RestoreCommand::restore)
                        )
        );
    }

    private static int restore(final CommandContext<ServerCommandSource> cc) {
        try (final UserLogger ulog = UserLogger.ulog(cc)) {
            gitOp(NONE, ulog, repo -> {
                final String snapshotName = cc.getLastChild().getArgument(ARGUMENT, String.class);
                repo.doRestoreLocalSnapshot(snapshotName, ulog);
            });
        }
        return SUCCESS;
    }
}
