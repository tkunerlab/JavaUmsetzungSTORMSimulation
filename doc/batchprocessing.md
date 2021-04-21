# Batch-Processing

## Configuration
Before running you calculations a configuration file needs to be created.
First of all a header is needed with the following structure:

```
[General]
Name = "Experiment" 
OutputPath = "../batch_out"
OutputTiffStack = 0
```

- Name: Global name under which the batch-processing runs. A folder is created if none exists
- OutputPath: Path where the output is stored
- OutputTiffStack: 1=Create Tiff files, 0=Do not create Tiff files (faster)

Next the general simulation settings need to be defined:

```
[Simulation]
RepeatExperiment = 2      
Reproducible = 1  
ViewStatus = 1
CalibrationFile = "examples/calibration_file/210225_CalibFile.txt"
NumThreads = 1   
```

- RepeatExperiment: How often the same parameter combination is repeated to collect statistics. Only makes sense if Reproducible=0
- Reproducible: 1=Choose the same random seed for every run, 0=random seed changes for every run
- ViewStatus: Just leave this at 1
- CalibrationFile: Path to the calibration file to create 3D tiffstacks
- NumThreads: Number of threads to use for parallel calculation

The batch-processing supports loading multiple models as shown below

```
#Put all models in this section
[Models]
examples/models/Microtubules.wimp
examples/models/Mitochondria.nff 
```

Lastly values/value ranges need to be defined for parameters of interest

```
#Place all parameters after here
[Parameters]
LabelingEfficiency = 10.0            #[%]
MeanAngleField = 90               #[°]
AngularDistribution = 0.0           #[°]
DetectionEfficiency = 100.0         #[%]
BackgroundLabel = 0.0               #[1/ym^3]
LabelLength = 15.0                  #[nm]
FluorophoresPerLabel = 1.0
DutyCycle = 0.0005
BleachConstant = 2.231e-5
RecordedFrames = 10000 
locPrecisionXY = 4.0                #[nm]
locPrecisionZ = 8.0                 #[nm]
PsfWidth = 647.0                    #[nm]
AveragePhotonNumber = 4000
EpitopeDensityL = 1.625             #for lines only [1/nm^2]
EpitopeDensity = 0.0167             #[1/nm^2]
RadiusOfFilaments = 12.5            #[nm]
PointSize = 4.0
LineWidth = 2.0
ApplyBleaching = 0
CoupleSigmaIntensity = 1
PixelToNmRatio = 133.0 
FrameRate = 30.0                    #[Hz]
DeadTime = 0.0                      #[s]
SigmaBg = 22.0
ConstantOffset = 200.0
EmGain = 10.0
QuantumEfficiency = 1.0             #[%]
WindowSizePSF = 10                  #[pix]
EmptyPixelsOnRim = 5                #[pix]
Na = 1.45
Focus = 400.0                       #[nm]
Defocus = 800.0                     #[nm]
TwoDPSF = 1                   #1=2D, 0=3D
ElectronPerAdCount = 4.81
MeanBlinkingTime = 0.05             #[s]
DistributePSFOverFrames = 1
EnsureSinglePSF = 0
MinIntensity = 1000
PixelSize = 10                      #[nm]
SigmaRendering = 10
BlueGreenOnly = 0
```

For each parameter which is not boolean (1/0) a range of values can be applied with the following syntax:

```
#Multiple values for parameter:
#param = [val1; val2; val3]
#Range of values for parameter:
#param = [startval...endval/(stepsize)]
#combination of both
#param = [val1; val2; startval...endval/(stepsize); val3]
```
