package de.hotkeyyy.simplefabricscoreboard.scoreboard

import de.hotkeyyy.simplefabricscoreboard.Simplefabricscoreboard
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket
import net.minecraft.network.packet.s2c.play.ScoreboardScoreUpdateS2CPacket
import net.minecraft.scoreboard.Scoreboard
import net.minecraft.scoreboard.ScoreboardCriterion
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.ScoreboardObjective
import net.minecraft.scoreboard.number.BlankNumberFormat
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*


class Simplescoreboard(name: String, displayName: Text, val server: MinecraftServer) {
    private val scoreboard = Scoreboard()
    private val players = listOf<String>()
    private val objective = ScoreboardObjective(
        scoreboard,
        name,
        ScoreboardCriterion.DUMMY,
        displayName,
        ScoreboardCriterion.RenderType.INTEGER,
        false,
        BlankNumberFormat.INSTANCE
    )
    private var lines = listOf<Text>()


    init {
    }

    fun setLines(vararg lines: Text) {
        this.lines = lines.toList()
    }

    fun updateLine(line: Int, content: Text) {
        if (line < 1) {
            Simplefabricscoreboard.logger.warn("Tried to update invalid Line: $line")
            return
        }


        lines = lines.toMutableList().apply { set(line - 1, content) }
        val index = lines.size - line
        val packet = createScoreUpdatePacket(index + 1, content)
        ScoreboardManager.playerBoard
            .filter { it.value == this }
            .mapNotNull { (uuid, _) ->
                server.playerManager.getPlayer(UUID.fromString(uuid))
            }
            .forEach { player ->
                player.networkHandler.sendPacket(packet)
            }
    }

    private fun sendAddPacketsToPlayer(player: ServerPlayerEntity) {
        player.networkHandler.sendPacket(createObjectiveUpdatePacket(ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE))
        player.networkHandler.sendPacket(createObjectiveUpdatePacket(ScoreboardObjectiveUpdateS2CPacket.ADD_MODE))
        player.networkHandler.sendPacket(
            ScoreboardDisplayS2CPacket(
                ScoreboardDisplaySlot.SIDEBAR,
                objective
            )
        )
        lines.forEachIndexed { index, text ->
            player.networkHandler.sendPacket(
                createScoreUpdatePacket(lines.size - index, text)
            )
        }
    }

    internal fun removePlayer(player: ServerPlayerEntity) {
        ScoreboardManager.playerBoard.remove(player.uuidAsString)
        player.networkHandler.sendPacket(
            ScoreboardObjectiveUpdateS2CPacket(
                objective,
                ScoreboardObjectiveUpdateS2CPacket.REMOVE_MODE
            )
        )
    }


    internal fun addPlayer(player: ServerPlayerEntity) {
        sendAddPacketsToPlayer(player)
    }

    internal fun createScoreUpdatePacket(line: Int, content: Text): ScoreboardScoreUpdateS2CPacket {
        return ScoreboardScoreUpdateS2CPacket(
            line.toString(),
            objective.name,
            line,
            Optional.ofNullable(content),
            Optional.ofNullable(BlankNumberFormat.INSTANCE)
        )
    }

    internal fun createObjectiveUpdatePacket(mode: Int): ScoreboardObjectiveUpdateS2CPacket {
        return ScoreboardObjectiveUpdateS2CPacket(
            objective,
            mode
        )
    }

    fun removeAllPlayers() {
        players.forEach { playerUUID ->
            server.playerManager.playerList.filter { it.uuidAsString.equals(playerUUID) }
                .forEach { serverPlayerEntity ->
                    removePlayer(serverPlayerEntity)

                }
        }
    }
}