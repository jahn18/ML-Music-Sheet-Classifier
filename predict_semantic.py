import argparse
from src.MusicClassifer import MusicClassifer


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='input to the machine learning model')
    parser.add_argument("-m", "--model", dest="model", help="The tuned hyperparameters of the machine learning mode", required=True)
    parser.add_argument("-f", "--file", dest="file", help="The input image you wish to classify", required=True)
    parser.add_argument("-v", "--vocabulary", dest="semantic_vocabulary", help="The semantic vocabulary used to classify the nodes", required=True)

    args = parser.parse_args()
    music = MusicClassifer(args.model, args.file, args.semantic_vocabulary)
    print(music.classifyMusicNotes())
