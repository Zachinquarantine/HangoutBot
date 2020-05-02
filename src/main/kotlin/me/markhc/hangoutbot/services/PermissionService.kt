package me.markhc.hangoutbot.services

import me.aberrantfox.kjdautils.api.annotation.Service
import me.aberrantfox.kjdautils.api.dsl.command.Command
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.services.PersistenceService
import me.markhc.hangoutbot.dataclasses.Configuration
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

enum class PermissionLevel {
    BotOwner,
    GuildOwner,
    Administrator,
    Staff,
    Everyone
}

val DEFAULT_REQUIRED_PERMISSION = PermissionLevel.Everyone

@Service
class PermissionsService(private val config: Configuration, private val botConfig: BotConfiguration, private val persistenceService: PersistenceService) {
    fun getCommandPermissionLevel(guild: Guild?, command: Command): PermissionLevel {
        if(guild != null) {
            config.getGuildConfig(guild).let {
                return it.commandPermission[command.names.first()] ?: command.requiredPermissionLevel
            }
        } else {
            return config.commandPermission[command.names.first()] ?: command.requiredPermissionLevel
        }
    }

    fun setCommandPermissionLevel(guild: Guild?, command: Command, permissionLevel: PermissionLevel) {
        if(guild != null) {
            config.getGuildConfig(guild).apply {
                commandPermission[command.names.first()] = permissionLevel
            }
        } else {
            config.commandPermission[command.names.first()] = permissionLevel
        }
        persistenceService.save(config)
    }

    fun hasClearance(guild: Guild?, user: User, requiredPermissionLevel: PermissionLevel): Boolean {
        val permissionLevel = guild?.getMember(user)?.let {
            it.getPermissionLevel()
        }

        return if(permissionLevel == null) {
            requiredPermissionLevel == PermissionLevel.Everyone
        } else {
            permissionLevel <= requiredPermissionLevel
        }
    }

    fun getPermissionLevel(member: Member) =
            member.getPermissionLevel().ordinal

    fun isCommandVisible(guild: Guild?, user: User, command: Command) =
            hasClearance(guild, user, getCommandPermissionLevel(guild, command))

    private fun Member.getPermissionLevel() =
            when {
                isBotOwner() -> PermissionLevel.BotOwner
                isGuildOwner() -> PermissionLevel.GuildOwner
                isAdministrator() -> PermissionLevel.Administrator
                isStaff() -> PermissionLevel.Staff
                else -> PermissionLevel.Everyone
            }

    private fun Member.isBotOwner() = user.id == botConfig.ownerId
    private fun Member.isGuildOwner() = isOwner
    private fun Member.isAdministrator() : Boolean {
        val guildConfig = config.getGuildConfig(this.guild.id)
        val adminRoles = guildConfig.rolePermissions
                .filter { it.value == PermissionLevel.Administrator }
                .map { it.key }
        val userRoles = this.roles.map { it.id }

        return adminRoles.intersect(userRoles).isNotEmpty()
    }
    private fun Member.isStaff(): Boolean {
        val guildConfig = config.getGuildConfig(this.guild.id)
        val adminRoles = guildConfig.rolePermissions
                .filter { it.value == PermissionLevel.Staff }
                .map { it.key }
        val userRoles = this.roles.map { it.id }

        return adminRoles.intersect(userRoles).isNotEmpty()
    }
}
