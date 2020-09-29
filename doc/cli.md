# Command line interface

* running 

```bash
java -jar STORMVis/target/suresim_cli.jar-with-dependencies.jar \
examples/cli_example/model_file.txt \
examples/cli_example/simulationParameters.json \
examples/cli_example/cli_output
```

will print out the following messge

```bash
>>> Path: xxx/tkunerlab_suresim/examples/cli_example/model_file.txt
>>> EPITOPES
>>> Took 0,062861 seconds to read linesNumber of objects: 0
>>> Number of objects in Array: 115
>>> SimulationParamterData loaded successfully from file examples/cli_example/simulationParameters.json
>>> Number localizations: 69
>>> Whole converting and simulation time: 0.162626497s
>>> -------------------------------------
>>> Worker finished
>>> Path to write project: examples/cli_example/cli_output/model_file.tif
>>> project name: model_file.tif
>>> zMax: 614.8651 zMin: -11.719569
>>> 3D Image rendered (32*32)
>>> 3D Image rendered (512*30)
```

and create the following output directory

```bash
examples/cli_output
├── model_file.png
├── model_file.tif
├── model_fileColorBar_zmin-11.719569_zmax603.14557.png
├── model_fileEpitopes.txt
├── model_fileFluorophorePos.txt
├── model_fileLocalizations.txt
├── model_fileLocalizationsForFRCAnalysisXY.txt
├── model_fileLocalizationsForFRCAnalysisXZ.txt
├── model_fileLocalizationsForFRCAnalysisYZ.txt
├── model_fileParameters.txt
├── model_fileblueCh.tif
├── model_filegreenCh.tif
└── model_fileredCh.tif
```


