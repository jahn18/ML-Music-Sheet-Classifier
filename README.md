# ML-Music-Sheet-Playback: 

Much of current technology in the music industry works at innovating sound, but there have been limited innovations in understanding the language of music. There exists no effective tool for the perception of musical notation, inhibiting the improvement of playback and visual music manipulation. This field of research is known as Optical Music Recognition (OMR).    

In this project, I developed a Pytorch-based machine-learning model that can play back music notes scanned from a musical score. The model was inspired by a paper published by J. Calvo-Zaragoza [1], where the author proposes an architecture composed of a Convolutional Recurrent Neural Network and a Connectionist Temporal Classification loss function. This model addresses the musical score (the input data) as a single unit rather than a sequence of isolated elements that must be classified separately. In other words, during the classification process, it does not require fine-grained details of every musical symbol, such as its specific shape and location, but a transcript describing the sequence of music symbols within the score. An example of this classification process is shown in the image below.

<img width="1366" alt="image" src="https://user-images.githubusercontent.com/59242538/132778601-4d230215-5304-4d7a-8059-a67b8d6f0767.png">

# Video Presentation 
https://www.youtube.com/watch?v=VK6B7G5JZXw&ab_channel=JeffZhai

