# TODO

### Loading & Saving Worlds
- World saving hasn't yet been started, odds are we'll need to mixin to a savehandler or something along those lines to inject a subfolder into a dimension folder.
- Some possible areas to look into: AnvilChunkLoader, ISaveHandler.

### Creating Worlds
- We'll be using a different approach than ChunkAssembler and ChunkDisassembler this time, currently the plan is to use a Factory based approach, e.g. SubWorldFactory.create(World parent, Vec3 instantiateAt, Entity associateWith, BlockMap blocks);
- A BlockMap (seen above) is essentially a temporary collection of stuff that can be turned into a subworld, it's essentially what was previously a LocatedBlockList, but much less bulky.
- Flood fill will be the default method of discovering blocks needed for a MovingWorld, devs will be able to implement their own "assembly" that creates a BlockMap that can be put into the SubWorldFactory.create method listed above.

### Functioning Worlds
- Worlds will by default function but this can be turned off.
- Functioning is defined as the following features: **Blocks are not static and can be changed, this includes adding and removing blocks as well as fully working tiles.**
- A goal but not yet decided feature is a sort of world "bridge", allowing blocks to interact outside of their subworld, what is confirmed as a feature that I expect to have implemented is parking, parking would essentially lock a subworld onto the grid until the player allows it to move again, when a subworld is parked it will act as part of the parent world with no limitations.

### Methods of interest
- DimensionManger.getWorld DONE
- MinecraftServer updateLightAndEntities IN PROGRESS
- AnvilChunkLoader, quite a bit in there we need to look at.