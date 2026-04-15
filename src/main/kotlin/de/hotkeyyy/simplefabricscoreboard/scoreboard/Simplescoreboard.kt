package de.hotkeyyy.simplefabricscoreboard.scoreboard

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.numbers.BlankFormat
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import java.util.*

/**
 * A lightweight sidebar scoreboard wrapper backed by direct packet updates.
 *
 * Instances keep an ordered list of [Component] lines and send the current state
 * to players that are associated through [ScoreboardManager].
 *
 * @property server the active Minecraft server used to resolve online players
 */
class Simplescoreboard(name: String, displayName: Component, val server: MinecraftServer) {
    private val scoreboard = Scoreboard()
    private val players = Collections.synchronizedSet(mutableSetOf<UUID>())
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

    /**
     * Appends a new line using lazily constructed content.
     *
     * This is equivalent to evaluating [content] immediately and passing the result
     * to [line].
     */
    fun line(content: () -> Component) {
        line(content.invoke())
    }

    /**
     * Appends [content] to the end of the scoreboard.
     *
     * The new entry is added after the current last line.
     */
    fun line(content: Component) = line(lines.size + 1, content)

    /**
     * Sets or creates the line at the given 1-based [line] index.
     *
     * Missing intermediate positions are filled with a blank literal component so the
     * requested line can exist. If adding the line changes the total number of lines,
     * all displayed scores are resent to keep their sidebar ordering in sync.
     *
     * Invalid values below `1` are ignored.
     */
    fun line(line: Int, content: Component) {
        if (line < 1) return

        lines = lines.toMutableList().apply {
            val index = line - 1
            while (index >= this.size) {
                add(Component.literal(" "))
            }
            set(index, content)
        }
        trimTrailingEmptyLines()
        refresh()
    }

    /**
     * Applies several scoreboard mutations in sequence using this instance as receiver.
     *
     * This is mainly a convenience DSL entrypoint for grouped [line] calls.
     */
    fun update(block: Simplescoreboard.() -> Unit) {
        block.invoke(this)
        trimTrailingEmptyLines()
        refresh()
    }

    internal fun refreshPlayer(player: ServerPlayer) {
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

    fun refresh() {
        getAffectedPlayers().forEach(::refreshPlayer)
    }

    private fun getAffectedPlayers(): List<ServerPlayer> {
        return synchronized(players) {
            players.mapNotNull(server.playerList::getPlayer)
        }
    }

    private fun trimTrailingEmptyLines() {
        lines = lines
            .dropLastWhile(::isEmptyLine)
    }

    private fun isEmptyLine(content: Component): Boolean {
        return content.string.isBlank()
    }

    internal fun removePlayer(player: ServerPlayer) {
        players.remove(player.uuid)
        ScoreboardManager.unregisterBoard(player, this)
        player.connection.send(
            ClientboundSetObjectivePacket(
                objective, ClientboundSetObjectivePacket.METHOD_REMOVE
            )
        )
    }


    internal fun addPlayer(player: ServerPlayer) {
        players.add(player.uuid)
        ScoreboardManager.registerBoard(player, this)
        refreshPlayer(player)
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
        val assignedPlayers = synchronized(players) { players.toList() }
        assignedPlayers.forEach { playerUuid ->
            server.playerList.getPlayer(playerUuid)?.let(::removePlayer)
        }
    }

    operator fun Component.unaryPlus() {
        line(this)
    }


}

/**
 * Creates and configures a [Simplescoreboard] using a small builder-style DSL.
 *
 * @param name the internal objective name
 * @param displayName the title shown in the sidebar
 * @param server the active Minecraft server
 * @param block configuration block used to add initial lines
 */
fun scoreboard(
    name: String,
    displayName: Component,
    server: MinecraftServer,
    block: Simplescoreboard.() -> Unit
): Simplescoreboard {
    val board = Simplescoreboard(name, displayName, server)
    block.invoke(board)
    return board
}
