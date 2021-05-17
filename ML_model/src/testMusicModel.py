from GetMusicData import MusicClassifer

music = MusicClassifer('music_classifer_model.pth', 'sample.png', 'vocabulary_semantic.txt')
print(music.classifyMusicNotes())
