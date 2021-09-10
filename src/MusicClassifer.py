import torch, torchvision, PIL
import pathlib
import PIL
import numpy
from src.model import MusicModel
from PIL import *


class MusicClassifer:
    def __init__(self, path_to_saved_model, path_to_image, path_to_vocabulary_semantic):
        self.vocabulary_size = 1781
        self.model = MusicModel(self.vocabulary_size)
        self.model.load_state_dict(torch.load(path_to_saved_model, map_location='cpu'))

        # Get the image
        xform = torchvision.transforms.Compose([torchvision.transforms.Resize(128), torchvision.transforms.ToTensor()])
        img = PIL.Image.open(path_to_image) # This is where it needs the path to the image.
        img = ImageOps.grayscale(img)
        img = ImageOps.invert(img)
        self.image = xform(img)
        self.image = self.image.unsqueeze(1)

        # Create the vocabulary Hashtable
        self.symbol2index = {}
        self.index2symbol = {}

        dict_file = open(path_to_vocabulary_semantic,'r')
        dict_list = dict_file.read().splitlines()
        word_idx = 0

        for word in dict_list:
            self.symbol2index[word] = word_idx
            self.index2symbol[word_idx] = word
            word_idx += 1

    def classifyMusicNotes(self):
        output, _ = self.model(self.image)
        output,_ = torch.max(output, 1, keepdim=True)
        def decode_predictions(preds):
            preds = preds.permute(1, 0, 2) # batch-size, timestamps, predictions
            preds = torch.softmax(preds, 2)
            preds = torch.argmax(preds, 2)
            preds = preds.detach().cpu().numpy()
            cap_preds = []
            for p in preds:
              temp = []
              for k in p:
                  k = k - 1
                  if (k == -1):
                    continue
                    #temp.append('-') # our blank symbol for the ctc function
                  else:
                    k += 1
                    temp.append(self.index2symbol[k])
              cap_preds.append(temp)
            return cap_preds
        predictions = decode_predictions(output)
        return predictions[0]

