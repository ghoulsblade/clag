clag
====

minecraft mod to slow down time in chunks causing lag

first aim is tileentities (machines), next should be entities (mobs+items).

Cpu time taken by ticking the tile entities is measured in regular intervals (not every tick),
and summed up per chunk to find which chunks are causing lag.
In laggy chunks time is slowed down by skipping ticks for tile entities.

tech: similar to tickprofiler and tickthreading, we replace World.loadedTileEntity by our own class that overides iteration behavior.
That way we do not need to actually modify vanilla code, so we do not need to make this a core-mod.

Possible conflict with tickprofiler and tickthreading, since the override the same var.
Best way to solve the tickprofiler conflict would be to suggest an interface to them to allow nesting of overrides.
In the meantime it should be sufficient to turn off clag by chat command temporarily while doing a tick measurement.
If TickThreading is in use (not possible for dw20 pack at the time of writing) clag might not needed, 
since tickthreading can be configured to use tick regions independently.
Might still be a problem if the remaining regions wait on one laggy region to complete.

