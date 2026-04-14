package de.hotkeyyy.simplefabricscoreboard.scoreboard

import de.hotkeyyy.simplefabricscoreboard.Simplefabricscoreboard
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.numbers.BlankFormat
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.network.protocol.game.GamePacketTypes
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import java.util.*


class Simplescoreboard(name: String, displayName: Component, val server: MinecraftServer) {
    private val scoreboard = Scoreboard()
    private val players = listOf<String>()
    private val objective = Objective(
        scoreboard,
        name,
        ObjectiveCriteria.DUMMY,
        displayName,
        ObjectiveCriteria.RenderType.INTEGER,
        false,
        BlankFormat.INSTANCE
    )
    private var lines = listOf<Component>()


    init {
    }

    fun setLines(vararg lines: Component) {
        GamePacketTypes.CLIENTBOUND_SET_OBJECTIVE
        GamePacketTypes.CLIENTBOUND_SET_OBJECTIVE
        this.lines = lines.toList()
    }

    fun updateLine(line: Int, content: Component) {
        if (line < 1) {
            Simplefabricscoreboard.logger.warn("Tried to update invalid Line: $line")
            return
        }


        lines = lines.toMutableList().apply {
            val index = line - 1
            while ((index) >= this.size) {
                add(Component.literal(""))
            }
            set(index, content)
        }
        val index = lines.size - line
        val packet = createScoreUpdatePacket(index, content)
        ScoreboardManager.playerBoard.filter { it.value == this }.mapNotNull { (uuid, _) ->
            server.playerList.getPlayer(UUID.fromString(uuid))
        }.forEach { player ->
            player.connection.send(packet)
        }
    }

    private fun sendAddPacketsToPlayer(player: ServerPlayer) {

        player.connection.send(createObjectiveUpdatePacket(ClientboundSetObjectivePacket.METHOD_REMOVE))
        player.connection.send(createObjectiveUpdatePacket(ClientboundSetObjectivePacket.METHOD_ADD))
        player.connection.send(
            ClientboundSetDisplayObjectivePacket(
                DisplaySlot.SIDEBAR, objective
            )
        )
        lines.forEachIndexed { index, text ->
            player.connection.send(
                createScoreUpdatePacket(lines.size - index, text)
            )
        }
    }

    internal fun removePlayer(player: ServerPlayer) {
        ScoreboardManager.playerBoard.remove(player.stringUUID)
        player.connection.send(
            ClientboundSetObjectivePacket(
                objective, ClientboundSetObjectivePacket.METHOD_REMOVE
            )
        )
    }


    internal fun addPlayer(player: ServerPlayer) {
        sendAddPacketsToPlayer(player)
    }

    internal fun createScoreUpdatePacket(line: Int, content: Component): ClientboundSetScorePacket {
        return ClientboundSetScorePacket(
            line.toString(),
            objective.name,
            line,
            Optional.ofNullable(content),
            Optional.ofNullable(BlankFormat.INSTANCE)
        )
    }

    internal fun createObjectiveUpdatePacket(mode: Int): ClientboundSetObjectivePacket {
        return ClientboundSetObjectivePacket(
            objective, mode
        )
    }

    fun removeAllPlayers() {
        players.forEach { playerUUID ->
            server.playerList.players.filter { it.stringUUID.equals(playerUUID) }.forEach { serverPlayerEntity ->
                removePlayer(serverPlayerEntity)

            }
        }
    }
}