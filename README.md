# AcceleratedRendering 加速渲染
This is a client side only entity rendering optimization MOD, aiming at improving performance when rendering large amount of entities 
or complex modded entities with significant amount of vertices with compute shaders on GPU while being compatible with shader packs, 
other MODs and their entities.

![benchmark.jpg](benchmark.jpg)

## 🍝Sponsorship
This MOD is almost done by myself and takes thousands of hours of my own time working and testing on it to be released.
Sponsorships from players can ensure the future development, innovation and optimization of this MOD. Thanks for everyone
that support this MOD! If you like it and want to support my work on development of AcceleratedRendering, please consider sponsor me at [爱发电](https://afdian.com/a/argon4w)

## ✨Why need this MOD
Minecraft has a poor immediate rendering system for rendering entities (including block entities) that is inherited from 
OpenGL immediate rendering mode that older versions of Minecraft uses. It transforms and uploads vertices on the **single render thread** on **CPU** 
every frame the entities are rendered, which takes a huge amount of time spending on these operations and left CPU and GPU idle with a very low FPS 
when rendering large amount of vertices.

## ⚙️How it works
AcceleratedRendering constructs a unique rendering pipeline that caches the "original" vertices (vertices before the transform) 
into meshes and transforms them parallel in GPU using compute shaders. Then draw the transformed vertices with the original shader. 
In this way, this MOD can make entity rendering much more efficient by moving off transforming stress from the CPU 
at the same time being compatible with shader packs (currently support Iris Shaders). All acceleration features can be disabled
for better compatibility.

## 🖥️Hardware Requirements
AcceleratedRendering requires OpenGL 4.6 to work properly for the usage of persistently mapped buffers and compute shaders.
Graphics cards like NVIDIA GT 400 Series and Intel HD Graphics 520/530 or newer will fit this requirement.
This MOD has been tested on NVIDIA GTX 1660Ti Max-Q, NVIDIA GTX 3070Ti Laptop, NVIDIA GTX 4090 Laptop, RX 580, RX 5600XT.
Mobile devices are **not currently supported**.

## 🛠️Configuration
Configuration file can be found in ``<your Minecraft>/.minecraft/config/acceleratedrendering-client.toml``. You can modify
acceleration features in this file or in game (some specific configurations needs to restart the game to take effect).
In game configuration editor can be found in ``Mods > Accelerated Rendering > Config``. 