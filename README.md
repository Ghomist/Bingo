# Minecraft Bingo

**Now you can play bingo game in Minecraft 1.17+ with this little plugin!!!**<br>
It's a plugin of bingo game in Minecraft.

## What is Bingo

### Rules

Every player will receive a bingo card with 25 items in a 5\*5 chart.<br>
When the game was start, everyone should try to collect items on bingo card.<br>
Depend on the game mode, there are different mechanics of winning the game.

### Other details

Check on [wikipedia](<https://en.wikipedia.org/wiki/Bingo_(American_version)>)! ([Chinese page here](https://zh.wikipedia.org/wiki/%E7%BE%8E%E5%BC%8F%E8%B3%93%E6%9E%9C))

## Requirement

**1.17.\* Spigot** servers (or forks such as Paper server) is needed.

## Commands

- `/bingo`<br>
  Main command of this plugin. You can use this command for getting some information.
- `/bingo setup [allcollect]|[shareinventory]`<br>
  Set up a new bingo game. Type in mode(s) after the command, **allcollect** and **shareinventory** is alternative.<br>
  **allcollect**: the game will be finished only when all the items on map was collected or when someone use /bingo shutdown<br>
  **shareinventory**: every player in game shares the same bingo card, which means you cannot finish a slot that someone else has finished.
- `/bingo join [red|blue|yellow|green]`<br>
  Join a team named after colours, which are supposed in Minecraft especially.
- `/bingo start`<br>
  Start the bingo game. This will remove all players' advancements as well as their potion effects, but give some boost equipment or effects.
- `/bingo check <player-name>`<br>
  Check other players' bingo map.
- `/bingo playerlist`<br>
  Show a list of players who are joined the bingo game.
- `/bingo shutdown`<br>
  Turn off the bingo game forcibly.
- `/bingo help`<br>
  Get some help of this plugin.

## Objects List

**_WIP_**. For plugin configuration, see [**config.yml**](src/main/resources/config.yml).
