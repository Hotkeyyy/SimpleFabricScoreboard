# SimpleFabricScoreboard

SimpleFabricScoreboard is a lightweight Kotlin library for Minecraft Fabric that manages sidebar scoreboards through direct packet updates.

It provides a small Kotlin-first API for:
- creating sidebar scoreboards
- updating lines dynamically
- assigning one scoreboard per player
- clearing scoreboards cleanly when needed

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("de.hotkeyyy:simplefabricscoreboard:1.0.5")
}
```

## Core API

The main entrypoints are:
- `scoreboard(...)` to create a scoreboard
- `ScoreboardManager.setPlayerScoreboard(...)` to assign it to a player
- `board.update { ... }` to modify lines later
- `ScoreboardManager.removePlayerScoreboard(...)` to remove it from a player
- `ScoreboardManager.clearAllBoards()` to remove all registered scoreboards

## Usage

Create a scoreboard and assign it to a player:

```kotlin
val board = scoreboard("test_board", Component.literal("Test Board"), server) {
    +Component.literal("Line 1")
    +Component.literal("Line 2")
    +emptyLine()
    +Component.literal("Line 3")
}

ScoreboardManager.setPlayerScoreboard(player, board)
```

You can also use `line(...)` explicitly:

```kotlin
val board = scoreboard("stats", Component.literal("Stats"), server) {
    line(Component.literal("Coins: 120"))
    line(Component.literal("Kills: 8"))
    line(5, Component.literal("Rank: Gold"))
}
```

Update an existing scoreboard:

```kotlin
board.update {
    line(2, Component.literal("Kills: 9"))
    +Component.literal("Session: Active")
}
```

Remove scoreboards:

```kotlin
ScoreboardManager.removePlayerScoreboard(player)
ScoreboardManager.clearAllBoards()
```

## Notes

- `line(index, component)` uses `1`-based indexing.
- Missing intermediate lines are filled automatically.
- Trailing blank lines are trimmed before refresh.
## Contributing

Issues and pull requests are welcome.
