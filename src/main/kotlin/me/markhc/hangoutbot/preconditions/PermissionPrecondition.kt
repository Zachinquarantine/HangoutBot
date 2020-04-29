package me.markhc.hangoutbot.preconditions

import me.aberrantfox.kjdautils.api.annotation.Precondition
import me.aberrantfox.kjdautils.extensions.jda.toMember
import me.aberrantfox.kjdautils.internal.command.*
import me.markhc.hangoutbot.extensions.requiredPermissionLevel
import me.markhc.hangoutbot.services.*
import net.dv8tion.jda.api.entities.Emote

@Precondition
fun produceHasPermissionPrecondition(permissionsService: PermissionsService) = precondition {
    val command = it.container[it.commandStruct.commandName]
    if(command == null) {
        it.message.addReaction("\u274C").queue()
        return@precondition Fail()
    }
    val requiredPermissionLevel = command.requiredPermissionLevel ?: DEFAULT_REQUIRED_PERMISSION
    val guild = it.guild!!
    val member = it.author.toMember(guild)!!

    if (!permissionsService.hasClearance(member, requiredPermissionLevel))
        return@precondition Fail("You do not have the required permissions to perform this action.")

    return@precondition Pass
}
