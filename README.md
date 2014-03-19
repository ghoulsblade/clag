clag
====

serverside minecraft mod to slow down time in chunks causing lag

download
====
[CLag-0.0.5.jar](releases/CLag-0.0.5.jar?raw=true)

about
====

CPU-time consumed by tile-entities is measured in regular intervals (not every tick),
and in laggy chunks time is slowed down by skipping ticks for tile-entities.
That way the players causing lag are punished by getting their machines slowed down,
and the rest of the server can run at full speed.
Players near laggy chunks regularly get a chat message informing them of the slowdown and the coords of the worst tile-entity to help them fix it.
The measurement is repeated regularly (every 30 sec by default), and if the chunk stopped causing lag, it is no longer slowed down.

tech: similar to tickprofiler and tickthreading, we replace World.loadedTileEntity by our own class that overides iteration behavior.
That way we do not need to actually modify vanilla code, so we do not need to make this a core-mod.

current status
====

* profiling and slowing for tile entities works
* 3 thresholds and slow factors can be configured, by default x4, x10, x20
* players near laggy chunks are given a chat message with coords of the worst tile-entity to help them fix it


chat commands
====

* /clag-info : for players, shows information about the current chunk
* /clag [subcommand] : admin only
* /clag minslow [A] [B] [C] : set time-per-chunk thresholds, in nanoseconds, A=x4 B=x10 C=x20 by default
* /clag worst : list coords and time of worst chunk
* /clag reload : reload config file
* /clag slow : force slowdown current chunk 
* /clag slow [X] : force slowdown current chunk for the next [X] ticks
* /clag warn : reset warning interval, player warnings will be sent on next profile tick
* /clag stop : uinstall hook, disables clag-profiling and slowing. allows use of tickprofiler-mod
* /clag start : reinstall hook
* /clag enable_tick : default = 1, 0 to disable custom tick code entirely
* /clag enable_slow : default = 1, 0 to disable the tick-skipping entirely
* /clag force_vanilla : default = 0, 1 to use vanilla tick code without any if-checks
* /clag safe_mode : 1 = warnings only, no slowing possible, the hook is installed only during a profile tick
* /clag debug : print some debug vars
* /clag blacklist : clear blacklist
* /clag blacklist [blockid] : add blockid to blacklist, blacklisted ids will never be slowed
* /clag reset : reset some counter vars for debug
* /clag pdump : dump current player positions in overworld to clag-pdump.txt



permissions
=== 

in MCPC, all commands are assigned a permission node base off the classpath to the command class.  
* clag.CLagCommandInfo = for players, so they can do /clag-info
* clag.CLagCommand = for admins, access /clag [subcommand]

ideas
====

* option to damage players in laggy chunks regularly
* option to destroy worst tile-entity if chunk-time is over a certain threshold
* option to kick players that stay near a laggy chunk over a certain treshold too long (anti-afk)
* block that emits redstone signal when tps is below a certain number -> allow players to turn off farms and big machines automatically

todo
====

* currently only tile-entities are profiled and slowed, next should be mobile entities (mobs, items)
* design patch for tickprofiler to allow nesting of tile-entity hooks to fix the conflict, will need tick-profiler patch too


possible conflicts with other mods 
====

Possible conflict with tickprofiler and tickthreading, since they override the same var (World.loadedTileEntityList).
Best way to solve the tickprofiler conflict would be to suggest an interface to them to allow nesting of overrides.
In the meantime it should be sufficient to turn off clag by chat command temporarily while doing a tick measurement.
If TickThreading is in use (not possible for dw20 pack at the time of writing) clag might not needed, 
since tickthreading can be configured to use tick regions independently.
Might still be a problem if the remaining regions wait on one laggy region to complete.


