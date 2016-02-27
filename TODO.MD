# TODO

### Loading & Saving Worlds
- ~~World saving hasn't yet been started, odds are we'll need to mixin to a savehandler or something along those lines to inject a subfolder into a dimension folder.~~ **DONE!**
- ~~Some possible areas to look into: AnvilChunkLoader, ISaveHandler.~~ **DONE!**

### Creating Worlds
- We'll be using a different approach than ChunkAssembler and ChunkDisassembler this time, currently the plan is to use a Factory based approach, e.g. SubWorldFactory.create(World parent, Vec3 instantiateAt, Entity associateWith, BlockMap blocks);
- A BlockMap (seen above) is essentially a temporary collection of stuff that can be turned into a subworld, it's essentially what was previously a LocatedBlockList, but much less bulky.
- Flood fill will be the default method of discovering blocks needed for a MovingWorld, devs will be able to implement their own "assembly" that creates a BlockMap that can be put into the SubWorldFactory.create method listed above.

### Functioning Worlds
- Worlds will by default function but this can be turned off.
- Functioning is defined as the following features: **Blocks are not static and can be changed, this includes adding and removing blocks as well as fully working tiles.**
- A goal but not yet decided feature is a sort of world "bridge", allowing blocks to interact outside of their subworld, what is confirmed as a feature that I expect to have implemented is parking, parking would essentially lock a subworld onto the grid until the player allows it to move again, when a subworld is parked it will act as part of the parent world with no limitations.

### Methods of interest
- ~~DimensionManger.getWorld~~ **DONE!**
- ~~MinecraftServer updateLightAndEntities~~ IRRELEVANT
- ~~AnvilChunkLoader, quite a bit in there we need to look at.~~ WORLD SAVING AND LOADING IS **DONE!**

### Networking
- Networking is currently the main area of interest, WorldClient is not implemented at all and needs to be implemented so we can get info to render things with.
- Packets for sending chunks if in the correct area needs to be done as well.
- Packets to handle updates needs to be done.

### Player Proxy
- EntityPlayerSP: Our local player, it sends packets to the server whether that be internal, dedicated, or whatever. Just a dummy that tells the server what the client is up to. Not what we want.
- EntityOtherPlayerMP: Displays info to clients about their companions on multiplayer, what they're holding, what they're wearing, what they're breaking, etc. More for visuals so we don't care.
- EntityPlayerMP: The one we need to mess with, this is what the server considers a player, it handles all the important logic based on packets from the client. Pretty important.