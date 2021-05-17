import torch, torchvision, PIL, numpy as np
import pathlib
import PIL
import numpy as np

class readImage:
    def __init__(self, path_to_image):
        self.imagee = PIL.Image.open(path_to_image)

    def get(self):
        return self.imagee
