#3DViewer

This project is extracted from [TeamFX's openjfx-8u-dev-rt](https://github.com/teamfx/openjfx-8u-dev-rt/tree/master/apps/samples/3DViewer).

It originally contains:
* an [importer for Collada (.dae) files](https://github.com/teamfx/openjfx-8u-dev-rt/blob/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/importers/dae/DaeImporter.java)
* an [importer for Maya (.maya) files](https://github.com/teamfx/openjfx-8u-dev-rt/blob/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/importers/maya/MayaImporter.java)
* an [importer for 3dsMax (.max) files](https://github.com/teamfx/openjfx-8u-dev-rt/blob/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/importers/max/MaxLoader.java)
* an [importer for Wavefront (.obj) files](https://github.com/teamfx/openjfx-8u-dev-rt/blob/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/importers/obj/ObjOrPolyObjImporter.java)
* an [application to display 3D objects](https://github.com/teamfx/openjfx-8u-dev-rt/blob/master/apps/samples/3DViewer/src/main/java/com/javafx/experiments/jfx3dviewer/Jfx3dViewerApp.java)

The goal of this repo is to improve the Collada importer, enabling Collada imports from Blender :
* improving the implementation of the Collada importer
* adding skeletal import from a Collada file
* adding animation import from a Collada file
* etc