#grid(columns: (1fr, 1fr, 1fr),
text(9pt)[
  *Multimedia & Graphics Programming* 
],
text(9pt)[
   #align(center)[*0271987\@up.edu.mx*]
],
align(right)[#text(9pt)[
  *Álvaro Gallo Cruz* 
]]
)
#set align(center)
#text(size: 20pt)[
  *John Carmack: Doom now runs in a 
  #link("https://doompdf.pages.dev/doom.pdf", )[#underline[pdf]]*
]
#set par(
  justify: true,
  leading: 0.70em,
)

#set align(left)
#set text(12pt)

I do know ths guy: inverse square root algorithm, doom, id software, game development and c++ enjoyer. John is smart, and kind of a rebel. He did not take the path of great education and flashy degrees, as he dropped out of college after only two semesters, so, according to SEP, I might be more prepared than him. Carmack represents the idea of geek genius, alongside with the art of changing and breaking things. He was a prodigy and obsessed with computers from an early age. He once broke into the computer lab because he wanted to use Apple II that was in there, which caused him to land at a juvenile home for a year. He went to the University of Missouri, but he thought that the program was very 'slow paced', so he drove to Lousiana to work for _SoftDisk_, a software subscription company which sent monthly digital magazines in diskettes.

At _SoftDisk_ he got better understanding of the 6502 assembly instruction set and architecture, alongside with console graphics management, which brought him to use (and abuse) it for performance reasons. This was not magic, he got a good grasp of memory allocation and registers in the cpu, so that he could make the computer things faster, and therefore, more things. He made smooth scrolling graphics possible in normal computers, at the time where only Nintendo's specialized hardware could. He made this by tricking a EGA video card, where he managed to only draw the edge pixels, lowering the memory usage that a full screen blit had.

He founded _Id Software_ with his friends from _SoftDisk_. He wanted to bring his ideas to interactive software, but after all that, in the 90s, 3D was too slow for normal computers, but again, he figured out a way to optimize this. After _Commander Keen_, a 2d ripoff of Mario Bros for MS-DOS, he started using 2d algorithms to make 3d graphics faster. He figured out he could use 2d ray casting for 2.5d rendering, by making use of perspectives and simplified geometry (only 90-degree angles). With this, he developed the _Doom_ and _Wolfenstein_ 3D games. After this, for _Quake_ he jumped to the full 3D world, where he introduced lightmaps, which implemented real looking lights with just a texture baked to where it was brightening, removing the need to calculate lights at runtime. He also made PVS, which is a way of culling geometry for different regions of the map, which drastically reduced the number of draw calls per frame.

The source code for these games is available for free, and this has been appraised as one of the must-read projects for every computer scientist. The code shows algorithms used in a way that no one thought about before, like the fast inverse square root, that was so good that later on, SSE extension to the x86 instruction set introduced it as a standard for all computers with that architecture (rsqrtss). Uses of Data Structures like the Binary Space Partioning trees for the scene graph, allowed games to instantly reject invisible walls for every camera (user POV) position.

Carmack worked at _Oculus_ until 2019, where he made contributions to the low-latency rendering to the oculus go. He now has his own AI company, trying to replace some difficult human labor with its creation. He said in an interview: "I can work 60 hours a week and don't feel anything, its great to feel young at this age". GOAT.
