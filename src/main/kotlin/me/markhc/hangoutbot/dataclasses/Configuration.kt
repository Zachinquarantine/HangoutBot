package me.markhc.hangoutbot.dataclasses

import me.aberrantfox.kjdautils.api.annotation.Data
import me.markhc.hangoutbot.services.PermissionLevel
import net.dv8tion.jda.api.entities.Guild

@Data("data/guilds.json", killIfGenerated = false)
data class Configuration(val guildConfigurations: MutableList<GuildConfiguration> = mutableListOf(),
                         var totalCommandsExecuted: Int = 0,
                         var prefix: String = "++",
                         val commandPermission: MutableMap<String, PermissionLevel> = mutableMapOf()) {
    fun getGuildConfig(guildId: String): GuildConfiguration {
        val guild = guildConfigurations.find { it.guildId == guildId }

        if(guild != null) {
            return guild
        }

        guildConfigurations.add(GuildConfiguration(guildId))

        return guildConfigurations.first { it.guildId == guildId }
    }
    fun getGuildConfig(guild: Guild) = getGuildConfig(guild.id)
}

data class GuildConfiguration(val guildId: String = "",
                              var welcomeEmbeds: Boolean = false,
                              var welcomeChannel: String = "",
                              var loggingChannel: String = "",
                              var muteRole: String = "",
                              var totalCommandsExecuted: Int = 0,
                              val grantableRoles: MutableMap<String, MutableList<String>> = mutableMapOf(),
                              val mutedUsers: MutableList<MuteEntry> = mutableListOf(),
                              val rolePermissions: MutableMap<String, PermissionLevel> = mutableMapOf(),
                              val commandPermission: MutableMap<String, PermissionLevel> = mutableMapOf())

data class MuteEntry(val user: String = "", val timeUntil: String = "")