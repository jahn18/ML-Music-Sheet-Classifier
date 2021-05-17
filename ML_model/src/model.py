import torch, torchvision, PIL, numpy as np
import pathlib
import PIL
import numpy as np

from torch.nn import functional as F
from torch import nn

from torch.nn import functional as F
from torch import nn

class MusicModel(nn.Module):
  def __init__(self, num_notes):
      super(MusicModel, self).__init__()
      self.conv_1 = nn.Conv2d(1, 32, kernel_size=(3,3), padding=(1,1))
      self.max_pool_1 = nn.MaxPool2d(kernel_size=(2,2))
      self.conv_2 = nn.Conv2d(32, 64, kernel_size=(3,3), padding=(1,1))
      self.max_pool_2 = nn.MaxPool2d(kernel_size=(2,2))
      self.conv_3 = nn.Conv2d(64, 128, kernel_size=(3,3), padding=(1,1))
      self.max_pool_3 = nn.MaxPool2d(kernel_size=(2,2))
      self.conv_4 = nn.Conv2d(128, 256, kernel_size=(3,3), padding=(1,1))
      self.max_pool_4 = nn.MaxPool2d(kernel_size=(2,2))

      self.linear_1 = nn.Linear(2048, 256)
      self.drop_1 = nn.Dropout(0.2)

      self.gru = nn.GRU(256, 128, bidirectional=True, num_layers=2, dropout=0.25, batch_first=True)
      self.output = nn.Linear(256, num_notes + 1)

  def forward(self, images, targets=None):
      bs, c, h, w = images.size()
      #print(bs, c, h, w)
      x = F.relu(self.conv_1(images))
      #print(x.size())
      x = self.max_pool_1(x)
      #print(x.size())
      x = F.relu(self.conv_2(x))
      #print(x.size())
      x = self.max_pool_2(x)
      #print(x.size()) # 1, 64, 32, 75 // b, f, h, w
      x = F.relu(self.conv_3(x))
      #print("3")
      #print(x.size())
      x = self.max_pool_3(x)
      #print(x.size()) # 1, 64, 32, 75 // b, f, h, w
      # Setup a permutation for the rnn layers
      x = x.permute(0, 3, 1, 2) # 1, 75, 64, 32 // we do this to look at the width of the image
      #print(x.size())
      x = x.view(bs, x.size(1), -1)
      #print(x.size())
      x = self.linear_1(x)
      x = self.drop_1(x)
      #print(x.size())
      x, _ = self.gru(x)
      #print(x.size())
      x = self.output(x)
      #print(x.size())
      x = x.permute(1, 0, 2) # timesteps, batchsize, values -> for CTC must go in this order
      #print(x.size())
      if targets is not None:
          log_softmax_values = F.log_softmax(x, 2)
          input_lengths = torch.full(
              size=(bs, ), fill_value=log_softmax_values.size(0),
              dtype=torch.int32
          )
          #print(input_lengths)
          target_lengths = torch.full(
              size=(bs, ), fill_value=targets.size(1),
              dtype=torch.int32
          )
          #print(target_lengths)
          ctc_loss = nn.CTCLoss(blank=0)(
              log_softmax_values, targets, input_lengths, target_lengths
          )
          p = torch.exp(-1*ctc_loss)
          focal_ctc_loss= torch.mul(torch.mul(0.5,torch.pow((1-p),2.0)),ctc_loss)
          loss = torch.mean(focal_ctc_loss)
          return x, loss
      return x, None
