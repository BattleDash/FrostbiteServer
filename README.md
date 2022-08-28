# <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/a/ab/Frostbite_logo.svg/1200px-Frostbite_logo.svg.png" alt="Frostbite Logo" width="200px"/> FrostbiteServer
### An open-source work-in-progress reverse-engineered dedicated server for games running on the Frostbite engine.
### When finished, this will be used to host dedicated servers for [Kyber](https://github.com/BattleDash/Kyber).

---

This is being built specifically for STAR WARS Battlefront II, but can be somewhat easily adapted to other games running on the engine, you will need knowledge of reverse engineering and the structural layout of the Frostbite engine (the BF3 Server/PDB is a good resource).

What's done:
* Main loop
* Frostbite Filesystem (CAS bundles, Superbundles)
* EBX logic parsing/loading
* Networking, using Netty (supports StreamManagers, NetworkableMessages, Chat)
* Level loading and Entity creation
* Connecting to the server
* Event/Message system
* Ghost (Player & Entity replication, network StreamManager unfinished, still bein reversed)

What's in-progress:
* InitFS Lua support for filesystem
* Frostbite settings system (Server.EnableHealthRegen, etc)
* EBX logic execution
* Remote administration page & RCON
* Terrain loading
* Level data & physical Entity networking
* Plugin system

As this is a work-in-progress, you will commonly notice unused code, which is meant for future systems.

Contributions are welcome, but please adhere to the naming conventions and code standards outlined in the BF3 PDB file, and to the best of your ability reverse engineer and replicate logic from games themselves, instead of creating your own logic for systems. The goal is to have feature and logic parity with real Frostbite servers.