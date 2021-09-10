import os
import sys
import verovio
import gzip
from music21 import *
from cairosvg import svg2png
import cairosvg
from bs4 import BeautifulSoup
import shutil

directory = "./Dataset"
file_paths = []

for root, subdirectories, files in os.walk(directory):
    for file in files:
        #print(os.path.join(root, file))
        path = os.path.join(root, file)
        if file.endswith('.krn'):
            file_paths.append(path)

for i in range(len(file_paths)):
    dirName = 'RealDataset/{0}'.format(i)
# Create target directory & all intermediate directories if don't exists
    os.makedirs(dirName)
    tk = verovio.toolkit()
    out = converter.parse(file_paths[i]).write('musicxml')
    os.replace(out, "RealDataset/{1}/{0}.xml".format(i, i))
    tk.loadFile("RealDataset/{1}/{0}.xml".format(i, i))

    xml_file = open("RealDataset/{1}/{0}.xml".format(i, i))
    soup = BeautifulSoup(xml_file, 'lxml')
    semantic_encoding = []
    semantic_encoding.append('clef-C4')
    def key_signature(key):
        if key == '0':
            return 'CM'
        elif key == '1':
            return 'GM'
        elif key == '2':
            return 'DM'
        elif key == '3':
            return 'AM'
        elif key == '4':
            return 'EM'
        elif key == '5':
            return 'BM'
        elif key == '6':
            return 'F#M'
        elif key == '7':
            return 'C#M'
        elif key == '-1':
            return 'FM'
        elif key == '-2':
            return 'BbM'
        elif key == '-3':
            return 'EbM'
        elif key == '-4':
            return 'AbM'
        elif key == '-5':
            return 'DbM'
        elif key == '-6':
            return 'GbM'
        elif key == '-7':
            return 'CbM'

    if soup.find('fifths') is None or soup.find('beats') is None or soup.find('beat-type') is None:
        shutil.rmtree(dirName)
        continue

    semantic_encoding.append('keySignature-{0}'.format(key_signature(soup.find('fifths').string)))
    semantic_encoding.append('timeSignature-{0}/{1}'.format(soup.find('beats').string, soup.find('beat-type').string))

    def get_note_type(note_type, note_duration):
        if note_duration == '7560':
            return 'eighth.'
        elif note_duration == '3780':
            return 'sixteenth.'
        elif note_duration == '15120':
            return 'quarter.'
        if note_type == '16th':
            return 'sixteenth'
        return note_type

    for note in soup.find_all('note'):
        if note.pitch is None:
            note_letter = note.rest.string
            note_octave = ""
        else:
            note_letter = note.pitch.step.string
            note_octave = note.pitch.octave.string
        if note.type is None:
            note_type = 'multirest-{0}'.format(int(note.duration.string) / 10080)
        else:
            note_type = get_note_type(note.type.string, note.duration.string)
        semantic_encoding.append('note-{0}{1}_{2}'.format(note_letter, note_octave, note_type))
    print(semantic_encoding)
    svg_string = tk.renderToSVG(1)
    svg2png(bytestring=svg_string,write_to='RealDataset/{1}/{0}.png'.format(i, i))
    from PIL import Image
    im = Image.open('RealDataset/{1}/{0}.png'.format(i, i))
    im.size  # (364, 471)
    im.getbbox()  # (64, 89, 278, 267)
    im2 = im.crop(im.getbbox())
    im2.size  # (214, 178)
    im2.save('RealDataset/{1}/{0}.png'.format(i, i))

    textfile = open("RealDataset/{1}/{0}.semantic".format(i, i), "w")
    for symbol in semantic_encoding:
        textfile.write(symbol + " ")
    textfile.close()
