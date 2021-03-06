# ML-Music-Sheet-Playback: 

Much of the current technology in the music industry works at innovating sound, but there have been limited innovations in understanding the language of music. One particular field of research, known as Optical Music Recognition (OMR), is attempting to bridge this gap by developing computational methods to read music notation within documents. The goal of OMR is to teach the computer to interpret sheet music and produce a machine-readable version of the score.

In this project, I developed a Pytorch-based machine-learning model that can transcribe music notes from a musical score. The model was inspired by Calvo-Zaragoza, a scientist specializing in OMR research: [End-to-End Neural Optical Music Recognition of Monophonic Scores](https://www.mdpi.com/2076-3417/8/4/606) (Applied Sciences Journal). The paper proposes an architectural model composed of a Convolutional Recurrent Neural Network and a Connectionist Temporal Classification loss function. Watch the presentation video below for more information:

[![Presentation](https://i.gyazo.com/76a7770d676bd2e18ce9b7d58f3859ab.png)](https://www.youtube.com/watch?v=VK6B7G5JZXw)

# Installation 

1. Ensure that Python>=3.9 (with pip) is installed

2. In the project directory, install the required packages. 
```
pip install -r requirements.txt
```

# How to Run

1. To classify your own music sheet, please go to the project directory and run the following script with the following parameters: 
```
python predict_semantic.py -m src/music_classifer_model.pth -v semantics/vocabulary_semantic.txt -f [input_image]
``` 

Caveats: 
- The score must be [monophonic](https://www.collinsdictionary.com/dictionary/english/monophonic). 
- Make sure the input image is clear and properly cropped. 

### Example Usage:
  1. Input the sample.png image provided in the sample_image directory. 
 ``` 
 python predict_semantic.py -m src/music_classifer_model.pth -v semantics/vocabulary_semantic.txt -f sample_image/sample.png
 ``` 
2. You should recieve the following output:
```
['clef-G2', 'keySignature-EbM', 'timeSignature-3/4', 'note-Bb5_quarter', 'note-Eb5_eighth', 'note-Bb5_eighth', 'note-C6_eighth', 'note-Bb5_eighth', 'note-Ab5_eighth', 'note-Ab5_eighth', 'note-Ab5_eighth', 'rest-sixteenth', 'note-Ab5_sixteenth', 'note-G5_sixteenth', 'note-Ab5_sixteenth', 'note-Bb5_sixteenth', 'note-Ab5_sixteenth', 'note-G5_sixteenth', 'note-Ab5_sixteenth']
```

