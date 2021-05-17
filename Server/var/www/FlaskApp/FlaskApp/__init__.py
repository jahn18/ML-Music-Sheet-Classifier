from flask import Flask, request, jsonify, json
import io
import sys
import PIL
import pathlib
sys.path.append('/var/www/FlaskApp/FlaskApp')
sys.path.append('/usr/local/lib/python3.6/dist-packages')
from testClass import myClass
from PIL import Image

app = Flask(__name__)


@app.route("/")
def hello():
    from ml5 import MusicClassifer
    from HorizSplit import Split
    image  = PIL.Image.open('/var/www/FlaskApp/FlaskApp/sample2.png')
    inst = Split(image)
    list = inst.horSplit(oneChannel = False, devi = 20)
    string = " "
    for img in list:
        music = MusicClassifer('/var/www/FlaskApp/FlaskApp/music_classifer_double_model.pth', img, '/var/www/FlaskApp/FlaskApp/vocabulary_semantic.txt')
        musicFile = music.classifyMusicNotes()
        string += " ".join(str(x) for x in musicFile)
        string += " "
    string +=str(len(list))
    instance = myClass('hi!!!!')
    return string
if __name__ == "__main__":
    app.run(debug=True)

@app.route('/image', methods=['POST', 'GET'])
def req():
    from ml5 import MusicClassifer
    from readImage import readImage
    from HorizSplit import Split
    if request.method == 'GET':
        return jsonify({'message': 'this is a get request'})
    if request.method == 'POST':
#        img_bytes = file.read()
#        string = 'helloeveryone'
##        imageBytes =  request.get_data()
#        return jsonify({'message':'hello'})
#        image = Image.open(io.BytesIO(image_bytes))
#        dataa = imageBytes.encode()

#        str = request.get_data()
#        image = Image.open(io.BytesIO(image_bytes))
#        f = open('/var/www/FlaskApp/FlaskApp/sample11.png', 'wb+')
#        f.close()
#        print(request.files)
##        image_bytes = request.get_data()
#        image_bytes = request.get_data()
        image_bytes = request.get_data()
#        new_image = image_bytes[182:]
        image = Image.open(io.BytesIO(image_bytes))
#        image  = PIL.Image.open('/var/www/FlaskApp/FlaskApp/sample.png')
#        image = image_bytes
        inst = Split(image)
        list = inst.horSplit(oneChannel = False, devi = 20)
        string = ""
        for img in list:
            music = MusicClassifer('/var/www/FlaskApp/FlaskApp/music_classifer_double_model.pth', img,'/var/www/FlaskApp/FlaskApp/vocabulary_semantic.txt')
            musicFile = music.classifyMusicNotes()
            string += " ".join(str(x) for x in musicFile)
            string += " "
        string2 =  string.replace(' ', '\t')
        return string2
#        return jsonify({'message':'Received something'})
    return jsonify({'message': 'this is a request'})
