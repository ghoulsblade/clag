clag
====

minecraft mod to slow down time in chunks causing lag

about
====

Cpu time taken by ticking the tile entities is measured in regular intervals (not every tick),
and summed up per chunk to find which chunks are causing lag.
In laggy chunks time is slowed down by skipping ticks for tile entities.

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
* /clag [subcmd] : admin only
* /clag stop : uinstall hook, disables clag-profiling and slowing. allows use of tickprofiler
* /clag start : reinstall hook
* /clag minslow [A] [B] [C] : set time-per-chunk thresholds, in nanoseconds, A=x4 B=x10 C=x20 by default
* /clag worst : list coords and time of worst chunk
* /clag reload : reload config file
* /clag slow : force slowdown current chunk 
* /clag slow [X] : force slowdown current chunk for the next [X] ticks
* /clag warn : reset warning interval, player warnings will be sent on next profile tick


ideas
====

* option to damage players in laggy chunks regularly
* option to destroy worst tile-entity if chunk-time is over a certain threshold
* block that emits redstone signal when tps is below a certain number -> allow players to turn off farms and big machines automatically

TODO
====

* currently only tile-entities are profiled and slowed, next should be mobile entities (mobs, items)
* design patch for tickprofiler to allow nesting of tile-entity hooks to fix the conflict, will need tick-profiler patch too


Possible conflicts with other mods 
====

Possible conflict with tickprofiler and tickthreading, since the override the same var.
Best way to solve the tickprofiler conflict would be to suggest an interface to them to allow nesting of overrides.
In the meantime it should be sufficient to turn off clag by chat command temporarily while doing a tick measurement.
If TickThreading is in use (not possible for dw20 pack at the time of writing) clag might not needed, 
since tickthreading can be configured to use tick regions independently.
Might still be a problem if the remaining regions wait on one laggy region to complete.


